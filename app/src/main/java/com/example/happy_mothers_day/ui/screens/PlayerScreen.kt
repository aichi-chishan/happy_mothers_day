package com.example.happy_mothers_day.ui.screens

import android.content.res.Configuration
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.happy_mothers_day.R
import com.example.happy_mothers_day.ui.components.RotatingVinyl
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.VinylBlack
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    autoPlay: Boolean = false,
    audioUri: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var playerReady by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(audioUri) {
        playerReady = false
        isPlaying = false
        mediaPlayer?.release()
        mediaPlayer = null

        val mp = try {
            if (!audioUri.isNullOrEmpty()) {
                val file = File(audioUri)
                if (file.exists()) {
                    MediaPlayer().apply {
                        setDataSource(audioUri)
                        prepare()
                        setOnCompletionListener { isPlaying = false }
                    }
                } else null
            } else {
                MediaPlayer.create(context, R.raw.mothers_day_audio)?.apply {
                    setOnCompletionListener { isPlaying = false }
                }
            }
        } catch (_: Exception) { null }

        if (mp == null) {
            hasError = true
        } else {
            hasError = false
            mediaPlayer = mp
            duration = mp.duration
        }
        playerReady = true
    }

    LaunchedEffect(playerReady, autoPlay) {
        if (playerReady && autoPlay && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    // Progress polling
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val mp = mediaPlayer ?: break
            try {
                currentPosition = mp.currentPosition
                if (!isSeeking) {
                    seekPosition = if (duration > 0) currentPosition.toFloat() / duration else 0f
                }
            } catch (_: Exception) { }
            delay(300)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(VinylBlack.copy(alpha = 0.9f), Color(0xFF0D0D0D))
                )
            )
    ) {
        val onPlayPause: () -> Unit = {
            val mp = mediaPlayer
            if (mp == null) { hasError = true }
            else {
                if (isPlaying) mp.pause() else mp.start()
                isPlaying = !isPlaying
            }
        }
        val onSeekStart: () -> Unit = { isSeeking = true }
        val onSeek: (Float) -> Unit = { fraction ->
            seekPosition = fraction
            currentPosition = (fraction * duration).toInt()
        }
        val onSeekEnd: () -> Unit = {
            isSeeking = false
            mediaPlayer?.seekTo(currentPosition)
        }

        if (isLandscape) {
            LandscapePlayer(isPlaying, hasError, audioUri, duration, currentPosition, seekPosition, isSeeking, onPlayPause, onSeekStart, onSeek, onSeekEnd)
        } else {
            PortraitPlayer(isPlaying, hasError, audioUri, duration, currentPosition, seekPosition, isSeeking, onPlayPause, onSeekStart, onSeek, onSeekEnd)
        }

        // Back button on top, in BoxScope
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier.padding(if (isLandscape) 12.dp else 16.dp).align(Alignment.TopStart).zIndex(10f)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = RosePinkLight, modifier = Modifier.size(if (isLandscape) 24.dp else 28.dp))
        }
    }
}

@Composable
private fun PortraitPlayer(
    isPlaying: Boolean,
    hasError: Boolean,
    audioUri: String?,
    duration: Int,
    currentPosition: Int,
    seekPosition: Float,
    isSeeking: Boolean,
    onPlayPause: () -> Unit,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "献给妈妈",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = RosePinkLight),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "For My Dear Mother",
            style = MaterialTheme.typography.bodyMedium.copy(color = RosePinkLight.copy(alpha = 0.6f), fontSize = 14.sp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        RotatingVinyl(isPlaying = isPlaying, size = 220.dp)

        Spacer(modifier = Modifier.height(32.dp))

        ProgressBar(
            duration = duration,
            currentPosition = currentPosition,
            seekPosition = seekPosition,
            isSeeking = isSeeking,
            onSeekStart = onSeekStart,
            onSeek = onSeek,
            onSeekEnd = onSeekEnd
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = hasError, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = if (audioUri.isNullOrEmpty()) "请将音频文件放入 res/raw/ 目录" else "音频文件不存在或无法播放",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0B0B0), textAlign = TextAlign.Center),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp),
            containerColor = RosePink,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when { hasError -> "无法播放"; isPlaying -> "正在播放..."; else -> "点击播放" },
            style = MaterialTheme.typography.bodyMedium.copy(color = RosePinkLight.copy(alpha = 0.7f))
        )
    }

}

@Composable
private fun LandscapePlayer(
    isPlaying: Boolean,
    hasError: Boolean,
    audioUri: String?,
    duration: Int,
    currentPosition: Int,
    seekPosition: Float,
    isSeeking: Boolean,
    onPlayPause: () -> Unit,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: vinyl
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RotatingVinyl(isPlaying = isPlaying, size = 180.dp)
        }

        // Right: title, progress, controls
        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "献给妈妈",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = RosePinkLight),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "For My Dear Mother",
                style = MaterialTheme.typography.bodyMedium.copy(color = RosePinkLight.copy(alpha = 0.6f), fontSize = 12.sp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            ProgressBar(
                duration = duration,
                currentPosition = currentPosition,
                seekPosition = seekPosition,
                isSeeking = isSeeking,
                onSeekStart = onSeekStart,
                onSeek = onSeek,
                onSeekEnd = onSeekEnd
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = hasError, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = if (audioUri.isNullOrEmpty()) "音频文件缺失" else "音频文件无法播放",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB0B0B0), textAlign = TextAlign.Center),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            FloatingActionButton(
                onClick = onPlayPause,
                modifier = Modifier.size(56.dp),
                containerColor = RosePink,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when { hasError -> "无法播放"; isPlaying -> "正在播放..."; else -> "点击播放" },
                style = MaterialTheme.typography.bodySmall.copy(color = RosePinkLight.copy(alpha = 0.7f))
            )
        }
    }

}

@Composable
private fun ProgressBar(
    duration: Int,
    currentPosition: Int,
    seekPosition: Float,
    isSeeking: Boolean,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = seekPosition,
            onValueChange = { onSeek(it) },
            onValueChangeFinished = onSeekEnd,
            modifier = Modifier.fillMaxWidth().height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = RosePink,
                activeTrackColor = RosePink,
                inactiveTrackColor = RosePinkLight.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(if (isSeeking) currentPosition else currentPosition),
                style = MaterialTheme.typography.bodySmall.copy(color = RosePinkLight.copy(alpha = 0.6f), fontSize = 11.sp)
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall.copy(color = RosePinkLight.copy(alpha = 0.6f), fontSize = 11.sp)
            )
        }
    }
}

private fun formatTime(ms: Int): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
