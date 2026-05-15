package com.example.happy_mothers_day

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.happy_mothers_day.nfc.NfcHelper
import com.example.happy_mothers_day.ui.screens.HomeScreen
import com.example.happy_mothers_day.ui.screens.PlayerScreen
import com.example.happy_mothers_day.ui.theme.HappyMothersDayTheme

class MainActivity : ComponentActivity() {

    private lateinit var nfcHelper: NfcHelper
    private var nfcCallback: (() -> Unit)? = null

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
    }

    override fun onResume() {
        super.onResume()
        refreshNfcReader()
    }

    override fun onPause() {
        super.onPause()
        nfcHelper.stopNfcReader()
    }

    private fun refreshNfcReader() {
        if (nfcHelper.isNfcAvailable()) {
            nfcHelper.startNfcReader {
                nfcCallback?.invoke()
            }
        }
    }
}

@Composable
fun MainApp(
    nfcHelper: NfcHelper,
    registerNfcCallback: ((() -> Unit) -> Unit)? = null
) {
    val navController = rememberNavController()

    val isNfcAvailable = remember { nfcHelper.isNfcSupported() }
    val isNfcEnabled = nfcHelper.isNfcAvailable()

    // Register NFC callback so that when a tag is discovered, we navigate to player
    DisposableEffect(navController) {
        registerNfcCallback?.invoke {
            navController.navigate("player?autoPlay=true") {
                popUpTo("home") { inclusive = false }
            }
        }
        onDispose {
            registerNfcCallback?.invoke {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                isNfcAvailable = isNfcAvailable,
                isNfcEnabled = isNfcEnabled,
                onNavigateToPlayer = {
                    navController.navigate("player?autoPlay=false") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        composable("player?autoPlay={autoPlay}") { backStackEntry ->
            val autoPlay = backStackEntry.arguments?.getString("autoPlay")?.toBoolean() ?: false
            PlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                autoPlay = autoPlay
            )
        }
    }
}
