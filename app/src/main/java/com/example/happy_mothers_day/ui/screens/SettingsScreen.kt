package com.example.happy_mothers_day.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happy_mothers_day.storage.TagAudioStorage
import com.example.happy_mothers_day.ui.theme.DeepRose
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.SoftPinkBg

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (uri: String) -> Unit,
    onMappingDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val storage = remember { TagAudioStorage(context) }
    var mappings by remember { mutableStateOf(storage.getAllMappings()) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = DeepRose)
                }
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = DeepRose),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLandscape) {
                LandscapeContent(
                    mappings = mappings,
                    onNavigateToLearning = onNavigateToLearning,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onDelete = { tagId ->
                        storage.removeMapping(tagId)
                        mappings = storage.getAllMappings()
                        onMappingDeleted()
                    }
                )
            } else {
                PortraitContent(
                    mappings = mappings,
                    onNavigateToLearning = onNavigateToLearning,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onDelete = { tagId ->
                        storage.removeMapping(tagId)
                        mappings = storage.getAllMappings()
                        onMappingDeleted()
                    }
                )
            }
        }
    }
}

@Composable
private fun PortraitContent(
    mappings: List<TagAudioStorage.TagEntry>,
    onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Button(
        onClick = onNavigateToLearning,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RosePink)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("学习模式 — 绑定新标签", fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "已绑定的标签",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepRose)
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (mappings.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
        ) {
            Text(
                text = "暂无绑定\n点击上方按钮添加",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp).fillMaxWidth()
            )
        }
    } else {
        MappingList(mappings, onNavigateToPlayer, onDelete)
    }
}

@Composable
private fun LandscapeContent(
    mappings: List<TagAudioStorage.TagEntry>,
    onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left: tag list
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "已绑定的标签",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepRose)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (mappings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "暂无绑定",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    )
                }
            } else {
                MappingList(mappings, onNavigateToPlayer, onDelete)
            }
        }

        // Right: learning button panel
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onNavigateToLearning,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePink)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("学习模式 — 绑定新标签", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "选择一个音频文件\n然后扫描NFC标签\n即可完成绑定",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center)
            )
        }
    }
}

@Composable
private fun MappingList(
    mappings: List<TagAudioStorage.TagEntry>,
    onNavigateToPlayer: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mappings, key = { it.tagId }) { entry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPlayer(entry.audioUri) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Nfc, contentDescription = null, tint = DeepRose, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.fileName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "标签: ${entry.tagId.take(8)}…",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    IconButton(onClick = { onDelete(entry.tagId) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.Gray)
                    }
                }
            }
        }
    }
}
