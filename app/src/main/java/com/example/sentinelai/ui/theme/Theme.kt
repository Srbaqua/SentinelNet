package com.example.sentinelai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Teal80,
    tertiary = Indigo80,
    onPrimary = Color(0xFF06233F),
    onSecondary = Color(0xFF003736),
    onTertiary = Color(0xFF1B1A5C),
    background = AppBackgroundDark,
    onBackground = Color(0xFFE6EEF8),
    surface = AppSurfaceDark,
    onSurface = Color(0xFFE6EEF8),
    surfaceVariant = Color(0xFF1B2633),
    onSurfaceVariant = Color(0xFFCAD6E5),
    outline = AppOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Teal40,
    tertiary = Indigo40,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    background = AppBackgroundLight,
    onBackground = Color(0xFF0F1721),
    surface = AppSurfaceLight,
    onSurface = Color(0xFF0F1721),
    surfaceVariant = Color(0xFFEAF0FA),
    onSurfaceVariant = Color(0xFF334155),
    outline = AppOutlineLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SentinelAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}