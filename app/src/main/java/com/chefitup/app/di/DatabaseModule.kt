package com.chefitup.app.di

import android.content.Context
import androidx.room.Room
import com.chefitup.app.data.local.ChefItUpDatabase
import com.chefitup.app.data.local.ChefItUpMigrations
import com.chefitup.app.data.local.dao.FavouriteDao
import com.chefitup.app.data.local.dao.GamificationDao
import com.chefitup.app.data.local.dao.MealPlanDao
import com.chefitup.app.data.local.dao.PendingActionDao
import com.chefitup.app.data.local.dao.RecentlyViewedDao
import com.chefitup.app.data.local.dao.RecipeDao
import com.chefitup.app.data.local.dao.SearchHistoryDao
import com.chefitup.app.data.local.dao.ShoppingItemDao
import com.chefitup.app.data.local.dao.UserPreferenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChefItUpDatabase =
        Room.databaseBuilder(
            context,
            ChefItUpDatabase::class.java,
            "chefitup.db"
        )
            .addMigrations(ChefItUpMigrations.MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides fun provideRecipeDao(db: ChefItUpDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideFavouriteDao(db: ChefItUpDatabase): FavouriteDao = db.favouriteDao()
    @Provides fun provideMealPlanDao(db: ChefItUpDatabase): MealPlanDao = db.mealPlanDao()
    @Provides fun provideShoppingItemDao(db: ChefItUpDatabase): ShoppingItemDao = db.shoppingItemDao()
    @Provides fun provideSearchHistoryDao(db: ChefItUpDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun providePendingActionDao(db: ChefItUpDatabase): PendingActionDao = db.pendingActionDao()
    @Provides fun provideGamificationDao(db: ChefItUpDatabase): GamificationDao = db.gamificationDao()
    @Provides fun provideUserPreferenceDao(db: ChefItUpDatabase): UserPreferenceDao = db.userPreferenceDao()
    @Provides fun provideRecentlyViewedDao(db: ChefItUpDatabase): RecentlyViewedDao = db.recentlyViewedDao()
}
