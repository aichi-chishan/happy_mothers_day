package com.example.happy_mothers_day.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happy_mothers_day.ui.theme.DeepRose
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.SoftPinkBg
import com.example.happy_mothers_day.ui.theme.WarmGold

@Composable
fun HomeScreen(
    isNfcAvailable: Boolean,
    isNfcEnabled: Boolean,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SoftPinkBg,
                            RosePinkLight,
                            RosePinkLight.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Floating hearts animation
        FloatingHearts()

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with menu button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "菜单",
                            tint = DeepRose,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("播放音频") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToPlayer()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.MusicNote, contentDescription = null)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Main title
            Text(
                text = "母亲节快乐",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = DeepRose
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "妈妈，我爱您",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = RosePink,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Happy Mother's Day",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = RosePink.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // NFC status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Nfc,
                        contentDescription = "NFC",
                        tint = if (isNfcAvailable && isNfcEnabled) RosePink else Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val (statusText, statusColor) = when {
                        !isNfcAvailable -> "手机不支持NFC功能\n可通过右上角菜单播放音频" to Color.Gray
                        !isNfcEnabled -> "NFC功能未开启\n请在设置中打开NFC" to WarmGold
                        else -> "NFC已就绪\n将手机靠近线圈即可播放" to RosePink
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Also show a direct button to access player
            if (!isNfcAvailable || !isNfcEnabled) {
                Button(
                    onClick = onNavigateToPlayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePink)
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("直接播放音频", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FloatingHearts() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_hearts")

    val hearts = remember {
        listOf(
            HeartData(0.08f, 14f, 3000),
            HeartData(0.25f, 10f, 4000),
            HeartData(0.42f, 16f, 3500),
            HeartData(0.58f, 12f, 4200),
            HeartData(0.75f, 15f, 3800),
            HeartData(0.92f, 9f, 4500),
            HeartData(0.15f, 11f, 3300),
            HeartData(0.85f, 13f, 3600),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            val animY by infiniteTransition.animateFloat(
                initialValue = 1.1f,
                targetValue = -0.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(heart.duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "heart_float_${heart.xFraction}"
            )

            val opacity by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(heart.duration / 2, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heart_opacity_${heart.xFraction}"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(
                        x = ((heart.xFraction - 0.5f) * 300).dp,
                        y = ((animY - 0.5f) * 500).dp
                    )
                    .size((heart.sizeDp * 2).dp)
            ) {
                val hr = size.width / 2
                val cx = center.x
                val cy = center.y

                drawCircle(Color(0xFFE91E63).copy(alpha = opacity), hr * 0.45f, Offset(cx - hr * 0.32f, cy - hr * 0.15f))
                drawCircle(Color(0xFFE91E63).copy(alpha = opacity), hr * 0.45f, Offset(cx + hr * 0.32f, cy - hr * 0.15f))
                val path = Path().apply {
                    moveTo(cx - hr * 0.7f, cy - hr * 0.05f)
                    lineTo(cx + hr * 0.7f, cy - hr * 0.05f)
                    lineTo(cx, cy + hr * 0.7f)
                    close()
                }
                drawPath(path, Color(0xFFE91E63).copy(alpha = opacity))
            }
        }
    }
}

private data class HeartData(
    val xFraction: Float,
    val sizeDp: Float,
    val duration: Int
)
