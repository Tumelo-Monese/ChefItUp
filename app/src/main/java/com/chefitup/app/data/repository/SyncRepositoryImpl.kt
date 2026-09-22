package com.chefitup.app.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chefitup.app.data.local.PendingActionType
import com.chefitup.app.data.local.dao.PendingActionDao
import com.chefitup.app.data.local.entity.PendingActionEntity
import com.chefitup.app.data.remote.ChefItUpApi
import com.chefitup.app.data.remote.NetworkMonitor
import com.chefitup.app.data.remote.dto.SyncFavouriteRequest
import com.chefitup.app.data.remote.dto.SyncMealPlanRequest
import com.chefitup.app.data.remote.dto.SyncShoppingRequest
import com.chefitup.app.data.sync.SyncWorker
import com.chefitup.app.domain.model.ApiOutcome
import com.chefitup.app.domain.repository.SyncRepository
import com.chefitup.app.util.NetworkErrorMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingActionDao: PendingActionDao,
    private val api: ChefItUpApi,
    private val networkMonitor: NetworkMonitor,
    private val gson: Gson
) : SyncRepository {

    override fun observePendingCount(): Flow<Int> = pendingActionDao.observeCount()

    override suspend fun enqueue(type: String, payloadJson: String, entityKey: String?) {
        if (entityKey != null) {
            pendingActionDao.deleteByKey(type, entityKey)
        }
        pendingActionDao.insert(
            PendingActionEntity(
                type = type,
                payloadJson = payloadJson,
                entityKey = entityKey,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
        if (networkMonitor.isOnline) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun syncNow(): ApiOutcome<Int> {
        if (!networkMonitor.isOnline) {
            return ApiOutcome.Failure(
                messageResId = com.chefitup.app.R.string.error_no_internet,
                isNetworkError = true
            )
        }
        return try {
            val processed = processPendingActions()
            ApiOutcome.Success(processed)
        } catch (t: Throwable) {
            ApiOutcome.Failure(
                messageResId = NetworkErrorMapper.messageResId(t),
                message = t.message,
                cause = t,
                isNetworkError = NetworkErrorMapper.isNetworkError(t)
            )
        }
    }

    override fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    suspend fun processPendingActions(): Int {
        val actions = pendingActionDao.getAll()
        var processed = 0
        for (action in actions) {
            val success = runCatching { dispatch(action) }.getOrDefault(false)
            if (success) {
                pendingActionDao.delete(action.id)
                processed++
            } else {
                pendingActionDao.update(
                    action.copy(
                        attemptCount = action.attemptCount + 1,
                        lastError = "sync_failed"
                    )
                )
                if (action.attemptCount + 1 >= MAX_ATTEMPTS) {
                    pendingActionDao.delete(action.id)
                }
            }
        }
        return processed
    }

    private suspend fun dispatch(action: PendingActionEntity): Boolean {
        val type = runCatching { PendingActionType.valueOf(action.type) }.getOrNull()
            ?: return true
        return when (type) {
            PendingActionType.ADD_FAVOURITE,
            PendingActionType.REMOVE_FAVOURITE -> {
                val recipeId = readString(action.payloadJson, "recipeId") ?: return true
                val syncAction =
                    if (type == PendingActionType.ADD_FAVOURITE) "add" else "remove"
                softSync {
                    api.syncFavourite(SyncFavouriteRequest(recipeId, syncAction))
                }
            }
            PendingActionType.UPSERT_MEAL,
            PendingActionType.DELETE_MEAL -> {
                val id = action.entityKey
                    ?: readString(action.payloadJson, "id")
                    ?: return true
                val syncAction =
                    if (type == PendingActionType.UPSERT_MEAL) "upsert" else "delete"
                softSync {
                    api.syncMealPlan(
                        SyncMealPlanRequest(id, action.payloadJson, syncAction)
                    )
                }
            }
            PendingActionType.UPSERT_SHOPPING,
            PendingActionType.DELETE_SHOPPING,
            PendingActionType.UPDATE_SHOPPING_CHECKED -> {
                val id = action.entityKey
                    ?: readString(action.payloadJson, "id")
                    ?: "batch"
                val syncAction = when (type) {
                    PendingActionType.DELETE_SHOPPING -> "delete"
                    PendingActionType.UPDATE_SHOPPING_CHECKED -> "check"
                    else -> "upsert"
                }
                softSync {
                    api.syncShopping(
                        SyncShoppingRequest(id, action.payloadJson, syncAction)
                    )
                }
            }
            PendingActionType.SYNC_GAMIFICATION -> true
        }
    }

    /**
     * Treats 404/501 as success so local-first data is not stuck when sync APIs
     * are not yet available on the backend.
     */
    private suspend fun softSync(block: suspend () -> retrofit2.Response<Unit>): Boolean {
        return try {
            val response = block()
            response.isSuccessful || response.code() == 404 || response.code() == 501
        } catch (e: HttpException) {
            e.code() == 404 || e.code() == 501
        } catch (_: Throwable) {
            false
        }
    }

    private fun readString(json: String, key: String): String? {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = runCatching {
            gson.fromJson<Map<String, Any>>(json, type)
        }.getOrNull() ?: return null
        return map[key]?.toString()
    }

    companion object {
        private const val MAX_ATTEMPTS = 8
    }
}
