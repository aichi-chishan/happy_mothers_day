package com.example.happy_mothers_day.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.happy_mothers_day.nfc.NfcHelper
import com.example.happy_mothers_day.storage.TagAudioStorage
import com.example.happy_mothers_day.ui.theme.DeepRose
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.SoftPinkBg

@Composable
fun NfcTagReaderScreen(
    nfcHelper: NfcHelper,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { TagAudioStorage(context) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    var scannedTagId by remember { mutableStateOf<String?>(null) }
    var isDefault by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }

    // Start scanning immediately
    LaunchedEffect(Unit) {
        isScanning = true
        nfcHelper.captureNextTag { tagId ->
            scannedTagId = tagId
            isScanning = false
            // Check if this is already the default tag
            isDefault = tagId == storage.getDefaultTagId()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colors = listOf(SoftPinkBg, RosePinkLight, RosePinkLight.copy(alpha = 0.5f)))
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = DeepRose)
                }
                Text(
                    "读取线圈数据",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = DeepRose),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isScanning && scannedTagId == null) {
                // Waiting for scan
                Icon(Icons.Filled.Nfc, contentDescription = null, tint = RosePink, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("请将NFC线圈靠近手机背面", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepRose))
                Spacer(modifier = Modifier.height(8.dp))
                Text("等待扫描……", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = RosePink, modifier = Modifier.size(32.dp))
            }

            scannedTagId?.let { tagId ->
                // Tag scanned, show info
                Icon(Icons.Filled.Nfc, contentDescription = null, tint = RosePink, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("扫描成功", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DeepRose))
                Spacer(modifier = Modifier.height(8.dp))
                Text("线圈ID (十六进制):", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                Spacer(modifier = Modifier.height(4.dp))

                // Tag ID card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tagId.uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isDefault) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RosePink.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = RosePink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("此线圈已被设为默认线圈\n无需学习即可播放默认歌曲", color = DeepRose, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            storage.setDefaultTagId(null)
                            isDefault = false
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("取消默认")
                    }
                } else {
                    Button(
                        onClick = {
                            storage.setDefaultTagId(tagId)
                            isDefault = true
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePink)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("设为默认线圈", fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "设为默认后，此线圈无需学习\n靠近即可播放默认歌曲",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        scannedTagId = null
                        isScanning = true
                        nfcHelper.captureNextTag { id ->
                            scannedTagId = id
                            isScanning = false
                            isDefault = id == storage.getDefaultTagId()
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("重新扫描")
                }
            }
        }
    }
}
