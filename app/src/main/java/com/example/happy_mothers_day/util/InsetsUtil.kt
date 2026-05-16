package com.example.happy_mothers_day.util

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun statusBarHeight(): Dp {
    return with(LocalDensity.current) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        } else {
            @Suppress("DEPRECATION")
            WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
        }
    }
}
