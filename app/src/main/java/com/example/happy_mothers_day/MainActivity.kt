package com.example.happy_mothers_day

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.happy_mothers_day.audio.AudioManager
import com.example.happy_mothers_day.media.MediaSessionManager
import com.example.happy_mothers_day.nfc.NfcHelper
import com.example.happy_mothers_day.storage.TagAudioStorage
import com.example.happy_mothers_day.ui.screens.HomeScreen
import com.example.happy_mothers_day.ui.screens.NfcLearningScreen
import com.example.happy_mothers_day.ui.screens.NfcTagReaderScreen
import com.example.happy_mothers_day.ui.screens.PlayerScreen
import com.example.happy_mothers_day.ui.screens.SettingsScreen
import com.example.happy_mothers_day.ui.theme.HappyMothersDayTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/** User's specific NFC tag, auto-plays default audio without learning */
private const val HARDCODED_DEFAULT_TAG_ID = "04669e70d32a81"

class MainActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var nfcCallback: ((String) -> Unit)? = null
    private lateinit var mediaSessionManager: MediaSessionManager

    private val handler = Handler(Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            // Attempt to restart reader every 2s — ensures takeover when NFC is turned on
            refreshNfcReader()
            handler.postDelayed(this, 2000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 35) {
            enableEdgeToEdge()
        }
        nfcHelper = NfcHelper(this)
        mediaSessionManager = MediaSessionManager(this)

        // Wire media session ↔ audio manager
        try {
            mediaSessionManager.audioCallback = object : MediaSessionManager.AudioCallback {
                override fun onPlay() { AudioManager.togglePause() }
                override fun onPause() { AudioManager.togglePause() }
                override fun onSeekTo(positionMs: Int) { AudioManager.seekToFraction(positionMs.toFloat() / AudioManager.duration) }
            }
        } catch (_: Exception) { }
        try {
            AudioManager.mediaCallback = object : AudioManager.MediaCallback {
            override fun onPlay(durationMs: Int) {
                mediaSessionManager.updatePlaybackState(true, 0L, durationMs.toLong())
                mediaSessionManager.updateMetadata("母亲节快乐", "Happy Mother's Day", durationMs.toLong())
            }
            override fun onPause(positionMs: Int, durationMs: Int) {
                mediaSessionManager.updatePlaybackState(false, positionMs.toLong(), durationMs.toLong())
            }
            override fun onPositionUpdate(positionMs: Int, durationMs: Int) {
                mediaSessionManager.updatePosition(positionMs.toLong(), durationMs.toLong())
            }
        } } catch (_: Exception) { }

        setContent {
            HappyMothersDayTheme {
                MainApp(
                    nfcHelper = nfcHelper,
                    registerNfcCallback = { callback ->
                        nfcCallback = callback
                        refreshNfcReader()
                    }
                )
            }
        }

        intent?.let { handleNfcIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        refreshNfcReader()
        handler.post(pollingRunnable) // Start periodic polling
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(pollingRunnable)
        nfcHelper.stopNfcReader()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSessionManager.release()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun refreshNfcReader() {
        if (nfcHelper.isNfcAvailable()) {
            nfcHelper.startNfcReader { tagId ->
                nfcCallback?.invoke(tagId)
            }
        }
    }

    private fun handleNfcIntent(intent: Intent) {
        val tagId = nfcHelper.extractTagId(intent)
        if (tagId != null) {
            nfcCallback?.invoke(tagId)
        }
    }
}

@Composable
fun MainApp(
    nfcHelper: NfcHelper,
    registerNfcCallback: (((String) -> Unit) -> Unit)? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tagStorage = remember { TagAudioStorage(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val isNfcAvailable = remember { nfcHelper.isNfcSupported() }
    var isNfcEnabled by remember { mutableStateOf(nfcHelper.isNfcAvailable()) }

    // Pre-populate built-in tag 2EA5892C → FLAC audio on first launch
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (tagStorage.getMapping("2EA5892C") == null) {
                val audioDir = File(context.filesDir, "audio")
                audioDir.mkdirs()
                val dest = File(audioDir, "a_mild_tale_untold.flac")
                if (!dest.exists()) {
                    context.resources.openRawResource(R.raw.a_mild_tale_untold).use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                tagStorage.saveMapping("2EA5892C", dest.absolutePath, "A Mild Tale Untold.flac")
            }
        }
    }

    // Refresh NFC status on every resume + ensure saved default tag is recognized
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNfcEnabled = nfcHelper.isNfcAvailable()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Periodic NFC status refresh for UI (reader restart is handled by Activity polling)
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            isNfcEnabled = nfcHelper.isNfcAvailable()
        }
    }

    // Register NFC callback
    DisposableEffect(navController) {
        registerNfcCallback?.invoke { tagId ->
            isNfcEnabled = nfcHelper.isNfcAvailable()

            // Normalize tag ID for comparison
            val normalized = tagId.lowercase()

            // 1. Check hardcoded default tag
            if (normalized == HARDCODED_DEFAULT_TAG_ID) {
                navController.navigate("player?autoPlay=true&uri=") {
                    popUpTo("home") { inclusive = false }
                }
                return@invoke
            }

            // 2. Check user-saved default tag (from tag reader screen)
            val savedDefault = tagStorage.getDefaultTagId()
            if (savedDefault != null && normalized == savedDefault.lowercase()) {
                navController.navigate("player?autoPlay=true&uri=") {
                    popUpTo("home") { inclusive = false }
                }
                return@invoke
            }

            // 3. Check learned mapping
            val mapping = tagStorage.getMapping(tagId)
            if (mapping != null) {
                val encodedUri = java.net.URLEncoder.encode(mapping.audioUri, "UTF-8")
                navController.navigate("player?autoPlay=true&uri=$encodedUri") {
                    popUpTo("home") { inclusive = false }
                }
            } else {
                // 4. Unknown tag — show message, don't play
                Toast.makeText(context, "未识别的NFC标签\n请在设置中学习或设为默认线圈", Toast.LENGTH_SHORT).show()
            }
        }
        onDispose {
            registerNfcCallback?.invoke { }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                isNfcAvailable = isNfcAvailable,
                isNfcEnabled = isNfcEnabled,
                onNavigateToPlayer = {
                    navController.navigate("player?autoPlay=false&uri=") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToPlayerUri = { encodedUri ->
                    navController.navigate("player?autoPlay=false&uri=$encodedUri") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToTagReader = {
                    navController.navigate("tag_reader") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                nfcHelper = nfcHelper,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLearning = {
                    navController.navigate("learning") {
                        popUpTo("settings") { inclusive = false }
                    }
                },
                onNavigateToPlayer = { uri ->
                    val encodedUri = java.net.URLEncoder.encode(uri, "UTF-8")
                    navController.navigate("player?autoPlay=false&uri=$encodedUri") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        composable("learning") {
            NfcLearningScreen(
                nfcHelper = nfcHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("tag_reader") {
            NfcTagReaderScreen(
                nfcHelper = nfcHelper,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("player?autoPlay={autoPlay}&uri={uri}") { backStackEntry ->
            val autoPlay = backStackEntry.arguments?.getString("autoPlay")?.toBoolean() ?: false
            val rawUri = backStackEntry.arguments?.getString("uri") ?: ""
            val decodedUri = if (rawUri.isNotEmpty()) java.net.URLDecoder.decode(rawUri, "UTF-8") else null
            PlayerScreen(
                onNavigateBack = { navController.popBackStack() },
                autoPlay = autoPlay,
                audioUri = decodedUri
            )
        }
    }
}
