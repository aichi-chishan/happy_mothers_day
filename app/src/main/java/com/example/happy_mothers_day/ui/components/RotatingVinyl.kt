package com.example.happy_mothers_day.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.happy_mothers_day.R
import com.example.happy_mothers_day.ui.theme.LabelRed
import com.example.happy_mothers_day.ui.theme.VinylBlack
import com.example.happy_mothers_day.ui.theme.VinylGroove
import kotlinx.coroutines.isActive

/**
 * @param rotationDurationMs  one revolution duration (ms). 20000=20s/rev, 5000=5s/rev.
 * @param seekFraction        current progress 0..1, drives rotation while seeking.
 * @param isSeeking           true = rotation tracks seekFraction directly (direction
 *                            follows drag: forward=clockwise, backward=counter-clockwise).
 */
@Composable
fun RotatingVinyl(
    isPlaying: Boolean,
    size: Dp = 280.dp,
    rotationDurationMs: Int = 20000,
    seekFraction: Float = 0f,
    isSeeking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val rotation = rememberVinylRotation(isPlaying, rotationDurationMs, seekFraction, isSeeking)

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            drawVinylDisc(size.toPx())
        }
        Image(
            painter = painterResource(id = R.drawable.vinyl_center),
            contentDescription = "照片",
            modifier = Modifier.size(size * 0.58f).clip(CircleShape).rotate(rotation),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun rememberVinylRotation(
    isPlaying: Boolean, durationMs: Int, seekFraction: Float, isSeeking: Boolean
): Float {
    val rotation = remember { Animatable(0f) }
    val currentSeekFraction = rememberUpdatedState(seekFraction)

    LaunchedEffect(isPlaying, durationMs, isSeeking) {
        if (isSeeking) {
            // Continuous tracking via snapshot-aware State
            snapshotFlow { currentSeekFraction.value }
                .collect { fraction ->
                    rotation.snapTo(fraction * 360f)
                }
        } else if (isPlaying) {
            val dur = durationMs.coerceIn(5000, 20000)
            while (isActive) {
                rotation.animateTo(rotation.value + 360f, tween<Float>(dur, easing = LinearEasing))
            }
        }
    }

    return rotation.value
}

private fun DrawScope.drawVinylDisc(totalSize: Float) {
    val cx = totalSize / 2; val cy = totalSize / 2; val vinylR = totalSize / 2
    drawCircle(VinylBlack, vinylR, Offset(cx, cy))
    drawCircle(color = Color.White.copy(alpha = 0.06f), radius = vinylR - 1.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
    val grooveStart = vinylR * 0.71f; val grooveEnd = vinylR * 0.90f; var r = grooveEnd
    while (r > grooveStart) { drawCircle(VinylGroove, r, Offset(cx, cy), style = Stroke(width = 0.5.dp.toPx())); r -= 3.5.dp.toPx() }
    drawCircle(LabelRed, vinylR * 0.68f, Offset(cx, cy))
    drawCircle(color = Color.White.copy(alpha = 0.12f), radius = vinylR * 0.66f, center = Offset(cx, cy), style = Stroke(width = 0.5.dp.toPx()))
    drawCircle(Color.White.copy(alpha = 0.2f), 3.dp.toPx(), Offset(cx, cy))
}
