package com.example.happy_mothers_day.ui.theme

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassUI {
    private const val PREFS_NAME = "ui_prefs"
    private const val KEY_ADVANCED = "advanced_ui"

    fun isAdvancedMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ADVANCED, false)
    }

    fun setAdvancedMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ADVANCED, enabled).apply()
    }
}

/** Glass background: translucent white + thin border + shadow */
fun Modifier.glassBackground(
    cornerRadius: Dp = 20.dp,
    alpha: Float = 0.18f,
    borderAlpha: Float = 0.25f
): Modifier = this
    .shadow(4.dp, RoundedCornerShape(cornerRadius), spotColor = Color.White.copy(alpha = 0.15f))
    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = borderAlpha)), RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = alpha))

/** Glass background alternative: darker tint for player screen */
fun Modifier.glassDarkBackground(
    cornerRadius: Dp = 20.dp,
    alpha: Float = 0.1f,
    borderAlpha: Float = 0.12f
): Modifier = this
    .shadow(4.dp, RoundedCornerShape(cornerRadius), spotColor = Color.White.copy(alpha = 0.08f))
    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = borderAlpha)), RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.Black.copy(alpha = alpha))

/** Adds blur effect for frosted glass look */
fun Modifier.glassBlur(radius: Dp = 10.dp): Modifier =
    this.blur(radius, BlurredEdgeTreatment.Unbounded)

/** Liquid shimmer overlay — animated diagonal light sweep */
@Composable
fun GlassShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glass_shimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent,
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent,
                    ),
                    start = Offset(shimmerOffset * 1000f, shimmerOffset * 1000f - 500f),
                    end = Offset(shimmerOffset * 1000f + 500f, shimmerOffset * 1000f)
                )
            )
    )
}

/** Advanced gradient background replacing the plain one */
fun glassGradient(isDark: Boolean = false): Brush = if (isDark) {
    Brush.radialGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F0F1A)
        )
    )
} else {
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF5F7),
            Color(0xFFFCE4EC),
            Color(0xFFF8BBD0).copy(alpha = 0.4f)
        )
    )
}
