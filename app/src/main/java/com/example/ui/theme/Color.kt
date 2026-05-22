package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

object AppColors {
    // BACKGROUNDS
    val bgPrimary    = Color(0xFFFFFFFF)  // main scaffold bg
    val bgSurface    = Color(0xFFF0FDF4)  // cards, sheets
    val bgCard       = Color(0xFFDCFCE7)  // mode cards, chips
    val bgInput      = Color(0xFFF8FAFC)  // input field bg

    // GREEN ACCENTS
    val green        = Color(0xFF22C55E)  // user bubble, primary buttons
    val greenDark    = Color(0xFF16A34A)  // icons, active borders, appbar elements
    val greenDeep    = Color(0xFF14532D)  // text ON green buttons/bubbles
    val greenGlow    = Color(0x2022C55E)  // shadows, glow effects

    // BORDERS
    val border       = Color(0xFFE2E8F0)  // card borders, dividers
    val borderFocus  = Color(0xFF22C55E)  // focused input

    // TEXT
    val textPrimary   = Color(0xFF0F172A)
    val textSecondary = Color(0xFF475569)
    val textMuted     = Color(0xFF64748B)
    val textHint      = Color(0xFF94A3B8)

    // CHAT BUBBLES
    // User: green gradient
    val userBubble = Brush.linearGradient(
        colors = listOf(Color(0xFF22C55E), Color(0xFF16A34A))
    )
    val userBubbleText = Color(0xFF052E16)
    val aiBubbleBg    = Color(0xFFFFFFFF)
    val aiBubbleBorder = Color(0xFFE2E8F0)
    val aiBubbleText  = Color(0xFF0F172A)

    // DANGER
    val danger = Color(0xFFEF4444)
}

// Backwards compatibility aliases styled to light values
val MatchaDark = AppColors.bgPrimary
val MatchaCard = AppColors.bgSurface
val MatchaBorder = AppColors.border
val MatchaGreenPrimary = AppColors.green
val MatchaGreenSecondary = AppColors.greenDark
val MatchaGreenDark = AppColors.greenDeep

val TextWhite = AppColors.textPrimary
val TextGray = AppColors.textSecondary
val TextMuted = AppColors.textMuted

val UserBubbleGradStart = Color(0xFF22C55E)
val UserBubbleGradEnd = Color(0xFF16A34A)

