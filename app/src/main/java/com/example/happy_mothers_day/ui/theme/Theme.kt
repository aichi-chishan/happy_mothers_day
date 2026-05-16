package com.example.happy_mothers_day.ui.theme

import android.app.Activity
import android.os.Build
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

private val LightColorScheme = lightColorScheme(
    primary = RosePink,
    onPrimary = CreamWhite,
    primaryContainer = RosePinkLight,
    onPrimaryContainer = DeepRose,
    secondary = WarmGold,
    onSecondary = DeepRose,
    secondaryContainer = WarmGoldLight,
    onSecondaryContainer = DeepRose,
    background = SoftPinkBg,
    onBackground = DeepRose,
    surface = CreamWhite,
    onSurface = DeepRose,
    surfaceVariant = RosePinkLight,
    onSurfaceVariant = RosePinkDark
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePinkLight,
    onPrimary = DeepRose,
    primaryContainer = RosePinkDark,
    onPrimaryContainer = RosePinkLight,
    secondary = WarmGoldLight,
    onSecondary = DeepRose,
    background = VinylBlack,
    onBackground = SoftPinkBg,
    surface = Color(0xFF2D2D2D),
    onSurface = SoftPinkBg
)

@Composable
fun HappyMothersDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                if (Build.VERSION.SDK_INT >= 35) {
                    // Edge-to-edge: transparent status bar, dark icons on light bg
                    @Suppress("DEPRECATION")
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.isStatusBarContrastEnforced = false
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                } else {
                    @Suppress("DEPRECATION")
                    window.statusBarColor = colorScheme.primary.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
            } catch (_: Exception) { }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
