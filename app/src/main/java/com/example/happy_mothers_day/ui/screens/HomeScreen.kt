package com.example.happy_mothers_day.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.happy_mothers_day.audio.AudioManager
import com.example.happy_mothers_day.ui.theme.DeepRose
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.SoftPinkBg
import com.example.happy_mothers_day.ui.theme.WarmGold
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    isNfcAvailable: Boolean,
    isNfcEnabled: Boolean,
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlayerUri: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTagReader: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var menuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // --- Mini player state ---
    var miniVisible by remember { mutableStateOf(false) }
    var miniPlaying by remember { mutableStateOf(false) }
    var miniDuration by remember { mutableIntStateOf(0) }
    var miniPosition by remember { mutableIntStateOf(0) }
    var miniFileName by remember { mutableStateOf("") }
    var miniSource by remember { mutableStateOf<String?>(null) }

    // Poll AudioManager for mini player
    LaunchedEffect(Unit) {
        while (true) {
            miniVisible = AudioManager.duration > 0
            miniPlaying = AudioManager.isPlaying
            miniDuration = AudioManager.duration
            AudioManager.pollPosition()
            miniPosition = AudioManager.currentPositionMs
            miniFileName = AudioManager.currentFileName
            miniSource = AudioManager.currentSourcePath
            delay(300)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftPinkBg, RosePinkLight, RosePinkLight.copy(alpha = 0.5f))
                    )
                )
        )

        FloatingHearts()

        // Menu button (top-right, on top of all content)
        Row(
            modifier = Modifier.fillMaxWidth().zIndex(100f).padding(top = statusBarHeight + 8.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "菜单", tint = DeepRose, modifier = Modifier.size(28.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("播放音频") },
                        onClick = { menuExpanded = false; onNavigateToPlayer() },
                        leadingIcon = { Icon(Icons.Filled.MusicNote, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("读取线圈数据") },
                        onClick = { menuExpanded = false; onNavigateToTagReader() },
                        leadingIcon = { Icon(Icons.Filled.Nfc, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("设置") },
                        onClick = { menuExpanded = false; onNavigateToSettings() },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("关于此APP") },
                        onClick = { menuExpanded = false; showAbout = true },
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                    )
                }

                // About dialog
                if (showAbout) {
                    AlertDialog(
                        onDismissRequest = { showAbout = false },
                        title = { Text("关于此APP", fontWeight = FontWeight.Bold, color = DeepRose) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("母亲节快乐", style = MaterialTheme.typography.titleMedium.copy(color = RosePink, fontWeight = FontWeight.Medium))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("这是献给妈妈的母亲节礼物", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Happy Mother's Day", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("作者: wxd", style = MaterialTheme.typography.bodySmall.copy(color = DeepRose, fontWeight = FontWeight.Medium))
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showAbout = false }) { Text("好的") }
                        }
                    )
                }
            }
        }

        if (isLandscape) {
            LandscapeLayout(isNfcAvailable, isNfcEnabled, onNavigateToPlayer, miniVisible)
        } else {
            PortraitLayout(isNfcAvailable, isNfcEnabled, onNavigateToPlayer, miniVisible)
        }

        // Mini player — floating above bottom (compact in landscape)
        if (miniVisible) {
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .padding(horizontal = if (isLandscape) 16.dp else 24.dp, vertical = 12.dp).zIndex(99f)) {
                MiniPlayer(
                fileName = miniFileName,
                isPlaying = miniPlaying,
                duration = miniDuration,
                position = miniPosition,
                source = miniSource,
                compact = isLandscape,
                onPlayPause = { AudioManager.togglePause(); miniPlaying = AudioManager.isPlaying },
                onSeek = { AudioManager.seekToFraction(it); miniPosition = AudioManager.currentPositionMs },
                onNavigateToPlayer = {
                    val uri = miniSource
                    if (uri != null) {
                        val encoded = java.net.URLEncoder.encode(uri, "UTF-8")
                        onNavigateToPlayerUri(encoded)
                    } else {
                        onNavigateToPlayer()
                    }
                }
            )
        }
    }
    }
}

@Composable
private fun MiniPlayer(
    fileName: String,
    isPlaying: Boolean,
    duration: Int,
    position: Int,
    source: String?,
    compact: Boolean = false,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val fraction = if (duration > 0) position.toFloat() / duration else 0f
    var seekDrag by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(fraction) }
    val displayFraction = if (seekDrag) dragFraction else fraction
    val displayMs = if (seekDrag) (dragFraction * duration).toInt() else position

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable(
                enabled = !seekDrag,
                onClickLabel = "查看播放详情"
            ) { onNavigateToPlayer() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(modifier = Modifier.padding(horizontal = if (compact) 12.dp else 20.dp, vertical = if (compact) 6.dp else 12.dp)) {
            // Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = RosePink,
                    modifier = Modifier.size(if (compact) 14.dp else 18.dp)
                )
                Spacer(modifier = Modifier.width(if (compact) 4.dp else 8.dp))
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))

            // Progress bar
            Slider(
                value = displayFraction,
                onValueChange = {
                    seekDrag = true
                    dragFraction = it
                },
                onValueChangeFinished = {
                    seekDrag = false
                    onSeek(dragFraction)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(if (compact) 20.dp else 28.dp),
                colors = SliderDefaults.colors(
                    thumbColor = RosePink,
                    activeTrackColor = RosePink,
                    inactiveTrackColor = RosePinkLight.copy(alpha = 0.3f)
                )
            )

            // Time + Play button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMiniTime(displayMs),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(if (compact) 32.dp else 40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = RosePink,
                        modifier = Modifier.size(if (compact) 22.dp else 28.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatMiniTime(duration),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )
            }
        }
    }
}

private fun formatMiniTime(ms: Int): String {
    if (ms <= 0) return "0:00"
    val sec = ms / 1000
    return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
}

@Composable
private fun PortraitLayout(isNfcAvailable: Boolean, isNfcEnabled: Boolean, onNavigateToPlayer: () -> Unit, miniVisible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = if (miniVisible) 100.dp else 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text("母亲节快乐", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp, color = DeepRose), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text("妈妈，我爱您", style = MaterialTheme.typography.titleLarge.copy(color = RosePink, fontWeight = FontWeight.Medium), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Happy Mother's Day", style = MaterialTheme.typography.titleMedium.copy(color = RosePink.copy(alpha = 0.7f)), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        if (!isNfcAvailable || !isNfcEnabled) {
            Button(onClick = onNavigateToPlayer, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = RosePink)) {
                Icon(Icons.Filled.MusicNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("直接播放音频", fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        NfcCard(isNfcAvailable, isNfcEnabled)
    }
}

@Composable
private fun LandscapeLayout(isNfcAvailable: Boolean, isNfcEnabled: Boolean, onNavigateToPlayer: () -> Unit, miniVisible: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = if (miniVisible) 65.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: text + button
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("母亲节快乐", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = DeepRose), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(6.dp))
                Text("妈妈，我爱您", style = MaterialTheme.typography.titleLarge.copy(color = RosePink, fontWeight = FontWeight.Medium, fontSize = 16.sp), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Happy Mother's Day", style = MaterialTheme.typography.titleMedium.copy(color = RosePink.copy(alpha = 0.7f), fontSize = 12.sp), textAlign = TextAlign.Center)
                if (!isNfcAvailable || !isNfcEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onNavigateToPlayer, modifier = Modifier.fillMaxWidth().height(36.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = RosePink)) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("直接播放音频", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }
            // Right: NFC card
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                NfcCard(isNfcAvailable, isNfcEnabled)
            }
        }
    }
}

@Composable
private fun NfcCard(isNfcAvailable: Boolean, isNfcEnabled: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Filled.Nfc, contentDescription = "NFC", tint = if (isNfcAvailable && isNfcEnabled) RosePink else Color.Gray, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            val (statusText, statusColor) = when {
                !isNfcAvailable -> "手机不支持NFC功能\n可通过右上角菜单播放音频" to Color.Gray
                !isNfcEnabled -> "NFC功能未开启\n请在设置中打开NFC" to WarmGold
                else -> "NFC已就绪\n将手机靠近线圈即可播放" to RosePink
            }
            Text(text = statusText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = statusColor), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

// --- FloatingHearts (unchanged) ---

@Composable
private fun FloatingHearts() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_hearts")
    val hearts = remember {
        listOf(
            HeartData(0.05f, 20f, 3000), HeartData(0.20f, 16f, 4000), HeartData(0.35f, 22f, 3500), HeartData(0.50f, 17f, 4200),
            HeartData(0.65f, 21f, 3800), HeartData(0.80f, 15f, 4500), HeartData(0.10f, 18f, 3300), HeartData(0.90f, 19f, 3600),
            HeartData(0.30f, 14f, 4100), HeartData(0.70f, 20f, 3900),
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            val animY by infiniteTransition.animateFloat(
                initialValue = 1.1f, targetValue = -0.1f,
                animationSpec = infiniteRepeatable(animation = tween(heart.duration, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
                label = "heart_float_${heart.xFraction}"
            )
            val opacity by infiniteTransition.animateFloat(
                initialValue = 0.3f, targetValue = 0.8f,
                animationSpec = infiniteRepeatable(animation = tween(heart.duration / 2, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
                label = "heart_opacity_${heart.xFraction}"
            )
            Canvas(
                modifier = Modifier.offset(x = ((heart.xFraction - 0.5f) * 450).dp, y = ((animY - 0.5f) * 600).dp).size((heart.sizeDp * 2.5f).dp)
            ) {
                val w = size.width
                val h = size.height
                // Canvas is square so w == h, heart is correctly proportioned
                val path = Path().apply {
                    moveTo(w / 2, h * 0.88f)
                    cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.15f, w / 2, h * 0.22f)
                    cubicTo(w * 0.9f, h * 0.15f, w * 0.9f, h * 0.5f, w / 2, h * 0.88f)
                    close()
                }
                drawPath(path, Color(0xFFE91E63).copy(alpha = opacity))
            }
        }
    }
}

private data class HeartData(val xFraction: Float, val sizeDp: Float, val duration: Int)
