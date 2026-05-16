package com.example.happy_mothers_day.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom progress bar with proper end-cap visibility.
 * The thumb always has clearance so the track's rounded ends are never obscured.
 */
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    thumbRadius: Dp = 10.dp,
    trackHeight: Dp = 4.dp,
    thumbColor: Color = Color.Red,
    activeTrackColor: Color = Color.Red,
    inactiveTrackColor: Color = Color.LightGray,
) {
    val density = LocalDensity.current
    val thumbPx = with(density) { thumbRadius.toPx() }
    val trackPx = with(density) { trackHeight.toPx() }
    val totalHeight = (thumbRadius * 2).coerceAtLeast(trackHeight + 8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { onValueChangeFinished?.invoke() },
                    onDragCancel = { },
                    onDrag = { change, _ ->
                        val w = size.width - thumbPx * 2
                        if (w > 0) {
                            val x = (change.position.x - thumbPx).coerceIn(0f, w)
                            onValueChange(x / w)
                        }
                        change.consume()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val w = size.width - thumbPx * 2
                    if (w > 0) {
                        val x = (offset.x - thumbPx).coerceIn(0f, w)
                        onValueChange(x / w)
                        onValueChangeFinished?.invoke()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = center.y
            val startX = thumbPx
            val endX = size.width - thumbPx
            val trackW = endX - startX

            // Track background with rounded caps
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(startX, y - trackPx / 2),
                size = Size(trackW, trackPx),
                cornerRadius = CornerRadius(trackPx / 2, trackPx / 2)
            )

            // Active portion
            val activeW = trackW * value.coerceIn(0f, 1f)
            if (activeW > 0f) {
                drawRoundRect(
                    color = activeTrackColor,
                    topLeft = Offset(startX, y - trackPx / 2),
                    size = Size(activeW, trackPx),
                    cornerRadius = CornerRadius(trackPx / 2, trackPx / 2)
                )
            }

            // Thumb
            val thumbX = startX + trackW * value.coerceIn(0f, 1f)
            drawCircle(color = thumbColor, radius = thumbPx, center = Offset(thumbX, y))
        }
    }
}
