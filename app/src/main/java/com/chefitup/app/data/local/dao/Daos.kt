package com.chefitup.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chefitup.app.data.local.entity.FavouriteEntity
import com.chefitup.app.data.local.entity.GamificationEntity
import com.chefitup.app.data.local.entity.MealPlanEntity
import com.chefitup.app.data.local.entity.PendingActionEntity
import com.chefitup.app.data.local.entity.RecentlyViewedEntity
import com.chefitup.app.data.local.entity.RecipeEntity
import com.chefitup.app.data.local.entity.SearchHistoryEntity
import com.chefitup.app.data.local.entity.ShoppingItemEntity
import com.chefitup.app.data.local.entity.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY cachedAtEpochMs DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<RecipeEntity>)

    @Query("DELETE FROM recipes WHERE cachedAtEpochMs < :olderThanEpochMs")
    suspend fun deleteOlderThan(olderThanEpochMs: Long)
}

@Dao
interface FavouriteDao {
    @Query("SELECT * FROM favourites ORDER BY savedAtEpochMs DESC")
    fun observeAll(): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE recipeId = :recipeId)")
    fun observeIsFavourite(recipeId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE recipeId = :recipeId)")
    suspend fun isFavourite(recipeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE recipeId = :recipeId")
    suspend fun delete(recipeId: String)
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan ORDER BY weekStartEpochDay, dayOfWeek, mealType")
    fun observeAll(): Flow<List<MealPlanEntity>>

    @Query(
        "SELECT * FROM meal_plan WHERE weekStartEpochDay = :weekStart " +
            "ORDER BY dayOfWeek, mealType"
    )
    fun observeWeek(weekStart: Long): Flow<List<MealPlanEntity>>

    @Query(
        "SELECT * FROM meal_plan WHERE weekStartEpochDay = :weekStart " +
            "AND dayOfWeek = :dayOfWeek AND mealType = :mealType"
    )
    suspend fun forMeal(weekStart: Long, dayOfWeek: Int, mealType: String): List<MealPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MealPlanEntity)

    @Query("DELETE FROM meal_plan WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM meal_plan WHERE weekStartEpochDay = :weekStart AND dayOfWeek = :dayOfWeek")
    suspend fun clearDay(weekStart: Long, dayOfWeek: Int)
}

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY category, name")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items")
    suspend fun getAll(): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ShoppingItemEntity>)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    suspend fun clearCompleted()

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAtEpochMs DESC LIMIT 20")
    fun observeRecent(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}

@Dao
interface PendingActionDao {
    @Query("SELECT * FROM pending_actions ORDER BY createdAtEpochMs ASC")
    suspend fun getAll(): List<PendingActionEntity>

    @Query("SELECT COUNT(*) FROM pending_actions")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: PendingActionEntity): Long

    @Update
    suspend fun update(entity: PendingActionEntity)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM pending_actions WHERE entityKey = :entityKey AND type = :type")
    suspend fun deleteByKey(type: String, entityKey: String)
}

@Dao
interface GamificationDao {
    @Query("SELECT * FROM gamification WHERE id = 1 LIMIT 1")
    fun observe(): Flow<GamificationEntity?>

    @Query("SELECT * FROM gamification WHERE id = 1 LIMIT 1")
    suspend fun get(): GamificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GamificationEntity)
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun get(): UserPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserPreferenceEntity)
}

@Dao
interface RecentlyViewedDao {
    @Query("SELECT * FROM recently_viewed ORDER BY viewedAtEpochMs DESC LIMIT 30")
    fun observeRecent(): Flow<List<RecentlyViewedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentlyViewedEntity)

    @Query(
        "DELETE FROM recently_viewed WHERE recipeId NOT IN (" +
            "SELECT recipeId FROM recently_viewed ORDER BY viewedAtEpochMs DESC LIMIT 30)"
    )
    suspend fun trim()
}
