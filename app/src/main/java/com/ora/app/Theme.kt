package com.ora.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OraColorScheme = darkColorScheme()

@Composable
fun OraTrackATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OraColorScheme,
        content = content,
    )
}
