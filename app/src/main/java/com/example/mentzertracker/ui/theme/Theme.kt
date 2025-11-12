package com.vincentlarkin.mentzertracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---- Your existing palette ----
private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = TextOnDark,
    secondary = RedSecondary,
    onSecondary = TextOnDark,
    background = BlackBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    error = RedSecondary,
    onError = TextOnDark
)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
)

@Composable
fun MentzerTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // No window/status/nav bar mutation here.
    // Edge-to-edge is enabled in Activity; icons/colors are chosen by the system.

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
