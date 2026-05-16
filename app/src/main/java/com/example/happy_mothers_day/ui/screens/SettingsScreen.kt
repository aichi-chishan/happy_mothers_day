package com.example.happy_mothers_day.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happy_mothers_day.nfc.NfcHelper
import com.example.happy_mothers_day.storage.TagAudioStorage
import com.example.happy_mothers_day.ui.theme.DeepRose
import com.example.happy_mothers_day.ui.theme.RosePink
import com.example.happy_mothers_day.ui.theme.RosePinkLight
import com.example.happy_mothers_day.ui.theme.SoftPinkBg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SettingsScreen(
    nfcHelper: NfcHelper,
    onNavigateBack: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (uri: String) -> Unit,
    onMappingDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val storage = remember { TagAudioStorage(context) }
    var mappings by remember { mutableStateOf(storage.getAllMappings()) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // --- Edit state ---
    var editingEntry by remember { mutableStateOf<TagAudioStorage.TagEntry?>(null) }
    var scanningNfc by remember { mutableStateOf(false) }
    var confirmDeleteTag by remember { mutableStateOf<String?>(null) }

    // File picker for audio replacement
    val audioReplacer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { contentUri ->
            val entry = editingEntry ?: return@rememberLauncherForActivityResult
            val fileName = getFileName(context, contentUri)
            scope.launch {
                val localPath = copyAudio(context, contentUri, fileName, entry.audioUri)
                storage.saveMapping(entry.tagId, localPath, fileName)
                mappings = storage.getAllMappings()
                editingEntry = null
            }
        }
    }

    fun refreshMappings() {
        mappings = storage.getAllMappings()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(SoftPinkBg, RosePinkLight, RosePinkLight.copy(alpha = 0.5f)))
        ))

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = statusBarHeight), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = DeepRose)
                }
                Text("设置", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = DeepRose), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLandscape) {
                LandscapeContent(mappings, onNavigateToLearning, onNavigateToPlayer, { confirmDeleteTag = it }, { editingEntry = it })
            } else {
                PortraitContent(mappings, onNavigateToLearning, onNavigateToPlayer, { confirmDeleteTag = it }, { editingEntry = it })
            }
        }
    }

    // --- Edit Dialog ---
    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null; scanningNfc = false },
            title = { Text("编辑绑定", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("音频: ${entry.fileName}", style = MaterialTheme.typography.bodySmall)
                    Text("标签: ${entry.tagId.take(12)}…", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    if (scanningNfc) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("请靠近新的NFC线圈...", color = RosePink, fontWeight = FontWeight.Medium)
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = RosePink)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        scanningNfc = true
                        nfcHelper.captureNextTag { newTagId ->
                            val normalized = newTagId.lowercase()
                            if (normalized == "04669e70d32a81" || storage.getDefaultTagId()?.lowercase() == normalized || (storage.getMapping(newTagId) != null && storage.getMapping(newTagId)!!.tagId != entry.tagId)) {
                                scanningNfc = false
                                editingEntry = null
                            } else {
                                val oldTagId = entry.tagId
                                storage.removeMapping(oldTagId)
                                storage.saveMapping(newTagId, entry.audioUri, entry.fileName)
                                refreshMappings()
                                scanningNfc = false
                                editingEntry = null
                            }
                        }
                    }) { Text(if (scanningNfc) "扫描中..." else "更换NFC") }
                    TextButton(onClick = {
                        audioReplacer.launch(arrayOf("audio/*"))
                    }) { Text("更换音频") }
                    TextButton(onClick = { editingEntry = null; scanningNfc = false }) { Text("取消") }
                }
            },
            dismissButton = null
        )
    }

    // --- Delete Confirmation Dialog ---
    confirmDeleteTag?.let { tagId ->
        val entry = mappings.find { it.tagId == tagId }
        AlertDialog(
            onDismissRequest = { confirmDeleteTag = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = {
                Text("确定要删除该绑定吗？\n\n音频: ${entry?.fileName ?: "未知"}\n标签: ${tagId.take(12)}…", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = {
                    storage.removeMapping(tagId)
                    refreshMappings()
                    onMappingDeleted()
                    confirmDeleteTag = null
                }) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTag = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PortraitContent(
    mappings: List<TagAudioStorage.TagEntry>, onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (String) -> Unit, onDelete: (String) -> Unit,
    onEdit: (TagAudioStorage.TagEntry) -> Unit
) {
    Button(onClick = onNavigateToLearning, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = RosePink)) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("学习模式 — 绑定新标签", fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
    Spacer(modifier = Modifier.height(24.dp))
    Text("已绑定的标签", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepRose))
    Spacer(modifier = Modifier.height(12.dp))
    if (mappings.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))) {
            Text("暂无绑定\n点击上方按钮添加", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray), textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp).fillMaxWidth())
        }
    } else {
        MappingList(mappings, onNavigateToPlayer, onDelete, onEdit)
    }
}

@Composable
private fun LandscapeContent(
    mappings: List<TagAudioStorage.TagEntry>, onNavigateToLearning: () -> Unit,
    onNavigateToPlayer: (String) -> Unit, onDelete: (String) -> Unit,
    onEdit: (TagAudioStorage.TagEntry) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text("已绑定的标签", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = DeepRose))
            Spacer(modifier = Modifier.height(8.dp))
            if (mappings.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))) {
                    Text("暂无绑定", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray), textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp).fillMaxWidth())
                }
            } else {
                MappingList(mappings, onNavigateToPlayer, onDelete, onEdit)
            }
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(onClick = onNavigateToLearning, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = RosePink)) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("学习模式 — 绑定新标签", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("选择一个音频文件\n然后扫描NFC标签\n即可完成绑定", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, textAlign = TextAlign.Center))
        }
    }
}

@Composable
private fun MappingList(
    mappings: List<TagAudioStorage.TagEntry>, onNavigateToPlayer: (String) -> Unit,
    onDelete: (String) -> Unit, onEdit: (TagAudioStorage.TagEntry) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mappings, key = { it.tagId }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToPlayer(entry.audioUri) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Nfc, contentDescription = null, tint = DeepRose, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.fileName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("标签: ${entry.tagId.take(8)}…", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }
                    IconButton(onClick = { onEdit(entry) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "编辑", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(entry.tagId) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && idx >= 0) name = cursor.getString(idx)
    }
    return name
}

private suspend fun copyAudio(context: Context, srcUri: Uri, fileName: String, oldPath: String): String = withContext(Dispatchers.IO) {
    // Delete old file
    try { File(oldPath).delete() } catch (_: Exception) { }
    val dir = File(context.filesDir, "audio")
    dir.mkdirs()
    val dest = resolveUniqueFile(dir, fileName)
    context.contentResolver.openInputStream(srcUri)?.use { input ->
        dest.outputStream().use { output -> input.copyTo(output) }
    }
    dest.absolutePath
}

private fun resolveUniqueFile(dir: File, name: String): File {
    val base = File(dir, name)
    if (!base.exists()) return base
    val dot = name.lastIndexOf('.')
    val stem = if (dot >= 0) name.substring(0, dot) else name
    val ext = if (dot >= 0) name.substring(dot) else ""
    var i = 1
    while (true) { val f = File(dir, "${stem}_$i$ext"); if (!f.exists()) return f; i++ }
}
