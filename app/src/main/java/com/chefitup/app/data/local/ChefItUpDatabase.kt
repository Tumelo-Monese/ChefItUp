package com.chefitup.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chefitup.app.data.local.dao.FavouriteDao
import com.chefitup.app.data.local.dao.GamificationDao
import com.chefitup.app.data.local.dao.MealPlanDao
import com.chefitup.app.data.local.dao.PendingActionDao
import com.chefitup.app.data.local.dao.RecentlyViewedDao
import com.chefitup.app.data.local.dao.RecipeDao
import com.chefitup.app.data.local.dao.SearchHistoryDao
import com.chefitup.app.data.local.dao.ShoppingItemDao
import com.chefitup.app.data.local.dao.UserPreferenceDao
import com.chefitup.app.data.local.entity.FavouriteEntity
import com.chefitup.app.data.local.entity.GamificationEntity
import com.chefitup.app.data.local.entity.MealPlanEntity
import com.chefitup.app.data.local.entity.PendingActionEntity
import com.chefitup.app.data.local.entity.RecentlyViewedEntity
import com.chefitup.app.data.local.entity.RecipeEntity
import com.chefitup.app.data.local.entity.SearchHistoryEntity
import com.chefitup.app.data.local.entity.ShoppingItemEntity
import com.chefitup.app.data.local.entity.UserPreferenceEntity

@Database(
    entities = [
        RecipeEntity::class,
        FavouriteEntity::class,
        MealPlanEntity::class,
        ShoppingItemEntity::class,
        SearchHistoryEntity::class,
        PendingActionEntity::class,
        GamificationEntity::class,
        UserPreferenceEntity::class,
        RecentlyViewedEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChefItUpDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun pendingActionDao(): PendingActionDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun recentlyViewedDao(): RecentlyViewedDao
}
