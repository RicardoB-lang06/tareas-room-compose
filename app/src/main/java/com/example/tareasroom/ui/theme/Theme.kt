package com.example.tareasroom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF185C46), onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EDE1), onPrimaryContainer = Color(0xFF103D2E),
    secondary = Color(0xFF596B60), background = Color(0xFFF7F8F2),
    surface = Color(0xFFF7F8F2), surfaceVariant = Color(0xFFE9EDE4),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA1D4B6), onPrimary = Color(0xFF083825),
    primaryContainer = Color(0xFF23513C), onPrimaryContainer = Color(0xFFD9EDE1),
    background = Color(0xFF111712), surface = Color(0xFF111712),
)

@Composable
fun TaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
