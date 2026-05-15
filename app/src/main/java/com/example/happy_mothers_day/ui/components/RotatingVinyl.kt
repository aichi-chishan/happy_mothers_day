package com.example.happy_mothers_day.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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

@Composable
fun RotatingVinyl(
    isPlaying: Boolean,
    size: Dp = 280.dp,
    modifier: Modifier = Modifier
) {
    val rotation by rememberVinylRotation(isPlaying)

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
private fun rememberVinylRotation(isPlaying: Boolean): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = if (isPlaying) {
            infiniteRepeatable(animation = tween<Float>(5000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
        } else {
            infiniteRepeatable(animation = tween<Float>(1, easing = LinearEasing), repeatMode = RepeatMode.Restart)
        },
        label = "rotation"
    )
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
