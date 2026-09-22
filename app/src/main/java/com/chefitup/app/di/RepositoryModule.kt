package com.chefitup.app.di

import com.chefitup.app.data.repository.FavouriteRepositoryImpl
import com.chefitup.app.data.repository.GamificationRepositoryImpl
import com.chefitup.app.data.repository.MealPlanRepositoryImpl
import com.chefitup.app.data.repository.RecipeRepositoryImpl
import com.chefitup.app.data.repository.ShoppingListRepositoryImpl
import com.chefitup.app.data.repository.SyncRepositoryImpl
import com.chefitup.app.data.repository.UserPreferencesRepositoryImpl
import com.chefitup.app.domain.repository.FavouriteRepository
import com.chefitup.app.domain.repository.GamificationRepository
import com.chefitup.app.domain.repository.MealPlanRepository
import com.chefitup.app.domain.repository.RecipeRepository
import com.chefitup.app.domain.repository.ShoppingListRepository
import com.chefitup.app.domain.repository.SyncRepository
import com.chefitup.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: MealPlanRepositoryImpl): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
