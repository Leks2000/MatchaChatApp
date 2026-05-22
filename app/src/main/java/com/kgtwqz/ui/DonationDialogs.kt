package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MatchaBorder
import com.example.ui.theme.MatchaCard
import com.example.ui.theme.MatchaDark
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.MatchaGreenSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DonationBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAmountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }, animationSpec = androidx.compose.animation.core.spring()),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }, animationSpec = androidx.compose.animation.core.spring()),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(width = 0.5.dp, color = MatchaBorder, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MatchaCard)
                    .clickable(enabled = false) {} // block click through
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Drag handle indicator
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Поддержи проект 🌿",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x0F000000), CircleShape)
                                .clickable { onDismiss() },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Matcha Chat полностью бесплатен. Твой донат идет напрямую на аренду серверов и оплату токенов нейросети. Никаких подписок — только добровольная поддержка.",
                        color = TextGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tier 1: 49 RUB
                    DonationTierCard(
                        title = "Матча Латве 🧉",
                        description = "Приятная чашка чая разработчику",
                        amountText = "49 ₽",
                        onClick = { onAmountSelected(49) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tier 2: 99 RUB
                    DonationTierCard(
                        title = "Фирменный Сет 🍵",
                        description = "Матча и уютное тепло серверам",
                        amountText = "99 ₽",
                        onClick = { onAmountSelected(99) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tier 3: 199 RUB
                    DonationTierCard(
                        title = "Целая Плантация 🌱",
                        description = "Бесперебойная и мощная работа ИИ",
                        amountText = "199 ₽",
                        onClick = { onAmountSelected(199) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "🔒 Secure",
                            tint = MatchaGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " Безопасные платежи через ЮKassa",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DonationTierCard(
    title: String,
    description: String,
    amountText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MatchaDark)
            .border(width = 0.5.dp, color = MatchaBorder, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            
            MatchaButton(
                text = amountText,
                onClick = onClick,
                cornerRadius = 12.dp
            )
        }
    }
}

/**
 * Beautiful YooKassa Secure Payment checkout simulator
 */
@Composable
fun YooKassaCheckoutModal(
    isActive: Boolean,
    amount: Int
) {
    if (!isActive) return

    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf(
        "Установка безопасного SSL-соединения...",
        "Опрос платежного шлюза ЮKassa...",
        "Создание транзакции на сумму $amount ₽...",
        "Авторизация 3D-Secure банка...",
        "Платеж подтвержден! Возврат в приложение..."
    )

    LaunchedEffect(isActive) {
        repeat(steps.size) { index ->
            currentStep = index
            delay(750) // Simulation stepping
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(width = 1.dp, color = MatchaGreenPrimary.copy(alpha = 0.3f), shape = RoundedCornerShape(24.dp))
                .background(MatchaCard)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // YooKassa Stylized Loading Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MatchaGreenPrimary, CircleShape)
                    )
                    Text(
                        text = " ЮKassa Secure",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                CircularProgressIndicator(
                    color = MatchaGreenPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = steps[currentStep],
                    color = TextGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.height(44.dp) // Maintain spacing
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Зашифрованное соединение SHA-256",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Visual success overlay presenting "Спасибо, ты крут 🌿", matching the confetti launch states
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DonationSuccessOverlay(
    isVisible: Boolean,
    amount: Int,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -100 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MatchaDark.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            // Background Confetti
            MatchaConfetti(isActive = isVisible)

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(width = 1.dp, color = MatchaGreenPrimary, shape = RoundedCornerShape(24.dp))
                    .background(MatchaCard)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Leaf Organic Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Brush.linearGradient(listOf(MatchaGreenPrimary, MatchaGreenSecondary)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌿",
                        fontSize = 44.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Спасибо, ты крут! 🌿",
                    color = com.example.ui.theme.AppColors.greenDark,
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = (-0.5).sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Мы успешно получили твой донат в размере $amount ₽. Твоя поддержка невероятно вдохновляет нас полировать Matcha Chat дальше.\n\nТеперь ты официально спонсор свободного общения! 🌱",
                    color = TextWhite,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                MatchaButton(
                    text = "Закрыть • Ура! 🎉",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
