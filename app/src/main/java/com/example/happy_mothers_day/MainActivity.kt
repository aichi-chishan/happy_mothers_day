package com.example.happy_mothers_day

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.happy_mothers_day.nfc.NfcHelper
import com.example.happy_mothers_day.storage.TagAudioStorage
import com.example.happy_mothers_day.ui.screens.HomeScreen
import com.example.happy_mothers_day.ui.screens.NfcLearningScreen
import com.example.happy_mothers_day.ui.screens.NfcTagReaderScreen
import com.example.happy_mothers_day.ui.screens.PlayerScreen
import com.example.happy_mothers_day.ui.screens.SettingsScreen
import com.example.happy_mothers_day.ui.theme.HappyMothersDayTheme

class MainActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var nfcCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        nfcHelper = NfcHelper(this)

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
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.stopNfcReader()
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

    // Refresh NFC status on every resume (user toggled NFC in quick-settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNfcEnabled = nfcHelper.isNfcAvailable()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Register NFC callback
    DisposableEffect(navController) {
        registerNfcCallback?.invoke { tagId ->
            isNfcEnabled = nfcHelper.isNfcAvailable()

            // Check default tag first
            val defaultTagId = tagStorage.getDefaultTagId()
            if (defaultTagId != null && tagId == defaultTagId) {
                navController.navigate("player?autoPlay=true&uri=") {
                    popUpTo("home") { inclusive = false }
                }
                return@invoke
            }

            // Check mapped tag
            val mapping = tagStorage.getMapping(tagId)
            if (mapping != null) {
                val encodedUri = java.net.URLEncoder.encode(mapping.audioUri, "UTF-8")
                navController.navigate("player?autoPlay=true&uri=$encodedUri") {
                    popUpTo("home") { inclusive = false }
                }
            } else {
                navController.navigate("player?autoPlay=true&uri=") {
                    popUpTo("home") { inclusive = false }
                }
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
