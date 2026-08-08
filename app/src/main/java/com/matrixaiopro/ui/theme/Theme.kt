package com.matrixaiopro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MatrixCyan,
    secondary = MatrixPurple,
    tertiary = MatrixAccent,
    background = MatrixDarkBg,
    surface = MatrixSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = MatrixOnSurface,
    onSurface = MatrixOnSurface
)

@Composable
fun MatrixAioProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
