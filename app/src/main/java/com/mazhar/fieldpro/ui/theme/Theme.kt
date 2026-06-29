package com.mazhar.fieldpro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = YellowPrimary,
    secondary = PurplePrimary,
    tertiary = GreenCompleted,
    background = Color(0xFF121200), // Dark gold tinted background
    surface = Color(0xFF1C1B08), // Dark gold surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outlineVariant = Color(0xFF3A3818)
)

private val LightColorScheme = lightColorScheme(
    primary = YellowPrimary,
    onPrimary = Color.Black,
    secondary = PurplePrimary,
    onSecondary = Color.Black,
    background = BackgroundLightColor,
    surface = Color.White,
    onBackground = TextDarkColor,
    onSurface = TextDarkColor,
    outlineVariant = CardBorderColor
)

@Composable
fun FieldProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to enforce our custom design colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}