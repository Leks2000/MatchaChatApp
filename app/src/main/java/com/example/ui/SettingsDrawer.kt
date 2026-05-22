package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMode
import com.example.ui.theme.MatchaBorder
import com.example.ui.theme.MatchaCard
import com.example.ui.theme.MatchaDark
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.MatchaGreenSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SettingsDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    activeMode: ChatMode,
    onSelectMode: (ChatMode) -> Unit,
    onClearHistory: () -> Unit,
    onTriggerDonate: () -> Unit,
    onSimulateDays: (Int) -> Unit,
    daysElapsed: Int,
    hasDonated: Boolean,
    totalDonated: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Transparent gray outer shadow dismissing drawer on click
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onClose() }
            )
        }

        // Animated draw container
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f) // Take up 85% of screen
                    .background(com.example.ui.theme.AppColors.bgPrimary)
                    .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.border)
                    .clickable(enabled = false) {}
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(com.example.ui.theme.AppColors.greenDark, CircleShape)
                            )
                            Text(
                                text = " Настройки",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x0F000000), CircleShape)
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MatchaBorder)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Modes Switch Area
                        Text(
                            text = "Личность собеседника:",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ChatMode.values().forEach { mode ->
                            val isSelected = mode == activeMode
                            val icon = when (mode) {
                                ChatMode.STANDARD -> "🌿"
                                ChatMode.MEME -> "🤪"
                                ChatMode.PSYCHOLOGIST -> "🌸"
                                ChatMode.GUIDE_MASTER -> "🎮"
                                ChatMode.BUSINESS -> "💼"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) com.example.ui.theme.AppColors.bgCard else com.example.ui.theme.AppColors.bgSurface)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isSelected) com.example.ui.theme.AppColors.greenDark else com.example.ui.theme.AppColors.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectMode(mode) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.title,
                                            color = if (isSelected) com.example.ui.theme.AppColors.greenDark else com.example.ui.theme.AppColors.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        Text(
                                            text = mode.description,
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MatchaBorder)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats & Donations status
                        Text(
                            text = "Твоя статистика поддержки:",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MatchaDark)
                                .border(width = 0.5.dp, color = MatchaBorder, shape = RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Регистрация:", color = TextGray, fontSize = 13.sp)
                                    Text(text = "$daysElapsed дней в сети", color = TextWhite, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Статус поддержки:", color = TextGray, fontSize = 13.sp)
                                    Text(
                                        text = if (hasDonated) "Спонсор 🌿" else "Пользователь",
                                        color = if (hasDonated) MatchaGreenPrimary else TextGray,
                                        fontSize = 13.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                                if (hasDonated) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Всего задоначено:", color = TextGray, fontSize = 13.sp)
                                        Text(text = "$totalDonated ₽", color = MatchaGreenPrimary, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MatchaBorder)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Panel
                        Text(
                            text = "Действия:",
                            color = TextGray,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MatchaDark)
                                .clickable { onClearHistory() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Очистить чат",
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MatchaDark)
                                .clickable { onTriggerDonate() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Heart",
                                tint = MatchaGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Поддержать проект 🌿",
                                color = MatchaGreenPrimary,
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MatchaBorder)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Test panel
                        Text(
                            text = "🧪 Проверка Донатов (Симуляция):",
                            color = MatchaGreenPrimary,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Позволяет мгновенно переместиться во времени для симуляции срабатывания триггера на 3, 6, 9 дни при старте приложения:",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SimulateButton("День 1", 1, onSimulateDays)
                            SimulateButton("День 3 🎯", 3, onSimulateDays)
                            SimulateButton("День 6 🎯", 6, onSimulateDays)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SimulateButton("День 9 🎯", 9, onSimulateDays)
                            SimulateButton("День 15 🎯", 15, onSimulateDays)
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                        
                        Text(
                            text = "Версия 1.0.0 (RuStore Distribution)\nАнти-NSFW Фильтр • Бесплатно навсегда",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulateButton(
    label: String,
    days: Int,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(com.example.ui.theme.AppColors.greenGlow)
            .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.greenDark.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            .clickable { onClick(days) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = com.example.ui.theme.AppColors.greenDark,
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
