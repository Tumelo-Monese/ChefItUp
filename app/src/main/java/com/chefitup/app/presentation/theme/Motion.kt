package com.chefitup.app.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/** Shared motion tokens — intentional, not noisy. */
object ChefMotion {
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    const val Fast = 220
    const val Medium = 380
    const val Slow = 560

    fun <T> emphasizedTween(duration: Int = Medium) =
        tween<T>(durationMillis = duration, easing = Emphasized)

    fun <T> softSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

val ChefEnterFadeSlide = fadeIn(ChefMotion.emphasizedTween()) +
    slideInVertically(
        animationSpec = ChefMotion.emphasizedTween(),
        initialOffsetY = { it / 12 }
    )

val ChefExitFadeSlide = fadeOut(tween(ChefMotion.Fast)) +
    slideOutVertically(
        animationSpec = tween(ChefMotion.Fast),
        targetOffsetY = { -it / 16 }
    )

val ChefScaleIn = fadeIn(ChefMotion.emphasizedTween(ChefMotion.Medium)) +
    scaleIn(
        animationSpec = ChefMotion.emphasizedTween(ChefMotion.Medium),
        initialScale = 0.92f
    )
