package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FpsHzColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = Color(0xFF041E28),
  primaryContainer = Color(0xFF083344),
  onPrimaryContainer = Color(0xFFA5F3FC),
  secondary = CyanSecondary,
  onSecondary = Color(0xFF0C243C),
  secondaryContainer = Color(0xFF075985),
  onSecondaryContainer = Color(0xFFE0F2FE),
  tertiary = CyanGlow,
  onTertiary = Color(0xFF00363D),
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceCard,
  onSurfaceVariant = TextSecondary,
  outline = SurfaceBorder,
  outlineVariant = SurfaceSubtle,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek dark mode as requested
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = FpsHzColorScheme,
    typography = Typography,
    content = content,
  )
}
