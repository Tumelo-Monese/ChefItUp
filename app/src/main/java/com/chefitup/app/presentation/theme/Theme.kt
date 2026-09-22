package com.chefitup.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.chefitup.app.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = FlameOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0D0),
    onPrimaryContainer = Color(0xFF3A1400),
    secondary = Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8ECDD),
    onSecondaryContainer = Color(0xFF0C2414),
    tertiary = EmberGold,
    onTertiary = Charcoal,
    tertiaryContainer = Color(0xFFFFE8B8),
    onTertiaryContainer = Color(0xFF3A2800),
    background = CreamCanvas,
    onBackground = Charcoal,
    surface = Color(0xFFFFFBFA),
    onSurface = Charcoal,
    surfaceVariant = WarmStone,
    onSurfaceVariant = CharcoalSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF9F5),
    surfaceContainer = WarmStone,
    surfaceContainerHigh = Color(0xFFE8DFD6),
    outline = Color(0xFF8A7A70),
    outlineVariant = Color(0xFFD6C8BE),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = FlameSoft,
    onPrimary = Color(0xFF3A1400),
    primaryContainer = DarkFlameContainer,
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = SageMuted,
    onSecondary = Color(0xFF0C2414),
    secondaryContainer = DarkSageContainer,
    onSecondaryContainer = Color(0xFFD4E8D8),
    tertiary = EmberGold,
    onTertiary = Charcoal,
    tertiaryContainer = Color(0xFF5C4200),
    onTertiaryContainer = Color(0xFFFFE8B8),
    background = DarkCanvas,
    onBackground = Color(0xFFF5EDE6),
    surface = DarkSurface,
    onSurface = Color(0xFFF5EDE6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD6C8BE),
    surfaceContainerLowest = Color(0xFF0A0807),
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = Color(0xFF3A332E),
    outline = DarkOutline,
    outlineVariant = Color(0xFF4A403A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun ChefItUpTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChefItUpTypography,
        shapes = ChefItUpShapes,
        content = content
    )
}
