package com.example.happy_mothers_day.ui.screens

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    autoPlay: Boolean = false,
    audioUri: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var playerReady by remember { mutableStateOf(false) }

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
                } else {
                    null
                }
            } else {
                MediaPlayer.create(context, R.raw.mothers_day_audio)?.apply {
                    setOnCompletionListener { isPlaying = false }
                }
            }
        } catch (e: Exception) {
            null
        }

        if (mp == null) {
            hasError = true
        } else {
            hasError = false
            mediaPlayer = mp
        }
        playerReady = true
    }

    LaunchedEffect(playerReady, autoPlay) {
        if (playerReady && autoPlay && mediaPlayer != null) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VinylBlack.copy(alpha = 0.9f),
                        Color(0xFF0D0D0D)
                    )
                )
            )
    ) {
        // Main content (bottom layer)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "献给妈妈",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RosePinkLight
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "For My Dear Mother",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RosePinkLight.copy(alpha = 0.6f),
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            RotatingVinyl(
                isPlaying = isPlaying,
                size = 260.dp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Error message if audio file not found
            AnimatedVisibility(
                visible = hasError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = if (audioUri.isNullOrEmpty())
                        "请将音频文件命名为 mothers_day_audio.mp3\n放入 res/raw/ 目录"
                    else
                        "音频文件不存在或无法播放\n请检查文件是否被移动或删除",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Play/Pause button
            FloatingActionButton(
                onClick = {
                    val mp = mediaPlayer
                    if (mp == null) {
                        hasError = true
                        return@FloatingActionButton
                    }
                    if (isPlaying) {
                        mp.pause()
                    } else {
                        mp.start()
                    }
                    isPlaying = !isPlaying
                },
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    hasError -> "无法播放"
                    isPlaying -> "正在播放..."
                    else -> "点击播放"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RosePinkLight.copy(alpha = 0.7f)
                )
            )
        }

        // Back button on top layer (drawn last, highest z-index)
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .zIndex(10f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = RosePinkLight,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
