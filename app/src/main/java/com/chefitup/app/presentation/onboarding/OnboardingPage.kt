package com.chefitup.app.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.chefitup.app.R

data class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    @DrawableRes val iconRes: Int
)

val onboardingPages = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_page1_title,
        bodyRes = R.string.onboarding_page1_body,
        iconRes = R.drawable.ic_onboarding_discover
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page2_title,
        bodyRes = R.string.onboarding_page2_body,
        iconRes = R.drawable.ic_onboarding_ingredients
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page3_title,
        bodyRes = R.string.onboarding_page3_body,
        iconRes = R.drawable.ic_onboarding_planner
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_page4_title,
        bodyRes = R.string.onboarding_page4_body,
        iconRes = R.drawable.ic_onboarding_chef
    )
)
