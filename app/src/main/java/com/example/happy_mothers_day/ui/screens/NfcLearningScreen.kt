package com.example.happy_mothers_day.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import android.provider.OpenableColumns

@Composable
fun NfcLearningScreen(
    nfcHelper: NfcHelper,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { TagAudioStorage(context) }

    var step by remember { mutableStateOf(1) } // 1=pick file, 2=scan tag, 3=done
    var selectedUri by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var capturedTagId by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it.toString()
            selectedFileName = getFileName(context, it)
            // Take persistable permission
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            step = 2
            // Start waiting for NFC tag
            nfcHelper.captureNextTag { tagId ->
                capturedTagId = tagId
                storage.saveMapping(tagId, selectedUri, selectedFileName)
                step = 3
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftPinkBg, RosePinkLight, RosePinkLight.copy(alpha = 0.5f))
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = DeepRose
                    )
                }
                Text(
                    text = "学习模式",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepRose
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Step indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepDot(1, step >= 1)
                Text(" ── ", color = if (step >= 2) RosePink else Color.LightGray)
                StepDot(2, step >= 2)
                Text(" ── ", color = if (step >= 3) RosePink else Color.LightGray)
                StepDot(3, step >= 3)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("选择音频", fontSize = 10.sp, color = DeepRose, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("扫描标签", fontSize = 10.sp, color = DeepRose, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("完成绑定", fontSize = 10.sp, color = DeepRose, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Main content per step
            when (step) {
                1 -> {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = DeepRose.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "第1步：选择一首音乐",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepRose
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "从手机中选择一个音频文件\n作为此NFC标签的专属音乐",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { filePicker.launch(arrayOf("audio/*")) },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePink)
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("选择音频文件", fontWeight = FontWeight.Medium)
                    }
                }

                2 -> {
                    // Pulse animation hint for NFC
                    Icon(
                        Icons.Filled.Nfc,
                        contentDescription = null,
                        tint = RosePink,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "第2步：靠近NFC标签",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = DeepRose
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "已选择: $selectedFileName",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = RosePink
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请将NFC标签（线圈贴纸、卡片等）\n靠近手机背面进行扫描",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }

                3 -> {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = RosePink,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "绑定成功！",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepRose
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedFileName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "↔",
                                style = MaterialTheme.typography.titleLarge.copy(color = RosePink)
                            )
                            Text(
                                text = "标签: ${capturedTagId.take(12)}…",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                step = 1
                                selectedUri = ""
                                selectedFileName = ""
                                capturedTagId = ""
                            },
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("再绑一个")
                        }
                        Button(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePink)
                        ) {
                            Text("完成")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDot(step: Int, active: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (active) RosePink else Color.LightGray,
        modifier = Modifier.size(if (active) 32.dp else 24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (active) {
                Text(
                    text = "$step",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun getFileName(context: Context, uri: android.net.Uri): String {
    var name = "unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
