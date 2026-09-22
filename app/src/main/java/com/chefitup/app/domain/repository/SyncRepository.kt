package com.chefitup.app.domain.repository

import com.chefitup.app.domain.model.ApiOutcome
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observePendingCount(): Flow<Int>
    suspend fun enqueue(
        type: String,
        payloadJson: String,
        entityKey: String? = null
    )

    suspend fun syncNow(): ApiOutcome<Int>
    fun schedulePeriodicSync()
}
