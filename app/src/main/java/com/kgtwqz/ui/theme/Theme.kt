package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = AppColors.green,
    onPrimary = AppColors.greenDeep,
    secondary = AppColors.greenDark,
    onSecondary = Color.White,
    tertiary = AppColors.greenDeep,
    background = AppColors.bgPrimary,
    onBackground = AppColors.textPrimary,
    surface = AppColors.bgSurface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.bgCard,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.border
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force false for Matcha light theme
  dynamicColor: Boolean = false, // Always use our custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

