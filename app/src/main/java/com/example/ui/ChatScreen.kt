package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMode
import com.example.data.MessageEntity
import com.example.ui.theme.MatchaBorder
import com.example.ui.theme.MatchaCard
import com.example.ui.theme.MatchaDark
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.MatchaGreenSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    var inputText by remember { mutableStateOf("") }
    var isDrawerOpen by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    // Auto scroll bottomwards upon message list expansions or streaming token updates
    LaunchedEffect(state.messages.size, state.streamingChunk) {
        val totalItems = state.messages.size + (if (state.isStreaming) 1 else 0)
        if (totalItems > 0) {
            lazyListState.animateScrollToItem(totalItems - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatchaDark)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            containerColor = MatchaDark,
            topBar = {
                // Customized Matcha Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(com.example.ui.theme.AppColors.bgPrimary)
                        .drawBehind {
                            drawLine(
                                color = com.example.ui.theme.AppColors.border,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left settings drawer trigger
                    val menuScale = remember { Animatable(1f) }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(menuScale.value)
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.example.ui.theme.AppColors.bgSurface)
                            .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.border, shape = RoundedCornerShape(16.dp))
                            .clickable {
                                scope.launch {
                                    menuScale.animateTo(0.92f, spring(stiffness = Spring.StiffnessHigh))
                                    menuScale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                                }
                                isDrawerOpen = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Меню",
                            tint = com.example.ui.theme.AppColors.greenDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Center Mode Info
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val uppercaseModeTitle = when (state.activeMode) {
                            ChatMode.STANDARD -> "РЕЖИМ: СТАНДАРТ 🌿"
                            ChatMode.MEME -> "РЕЖИМ: МЕМЫ 🤪"
                            ChatMode.PSYCHOLOGIST -> "РЕЖИМ: ПСИХОЛОГ 🌸"
                            ChatMode.GUIDE_MASTER -> "РЕЖИМ: ГИД 🎮"
                            ChatMode.BUSINESS -> "РЕЖИМ: БИЗНЕС 💼"
                        }
                        
                        Text(
                            text = uppercaseModeTitle,
                            color = com.example.ui.theme.AppColors.greenDark,
                            fontSize = 10.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            style = androidx.compose.ui.text.TextStyle(
                                letterSpacing = 1.2.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = "Матча ИИ 🌿",
                            color = com.example.ui.theme.AppColors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            style = androidx.compose.ui.text.TextStyle(
                                letterSpacing = (-0.2).sp
                            )
                        )
                    }

                    // Direct Support Badge (Donate)
                    val donateScale = remember { Animatable(1f) }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(donateScale.value)
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.example.ui.theme.AppColors.bgSurface)
                            .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.border, shape = RoundedCornerShape(16.dp))
                            .clickable {
                                scope.launch {
                                    donateScale.animateTo(0.92f, spring(stiffness = Spring.StiffnessHigh))
                                    donateScale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                                }
                                viewModel.showDonationPromptNow()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Поддержать",
                            tint = com.example.ui.theme.AppColors.green,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Messages lazy column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (state.messages.isEmpty() && !state.isStreaming) {
                        EmptyChatPlaceholder(
                            mode = state.activeMode,
                            onSendPreset = { presetText ->
                                viewModel.sendMessage(presetText)
                            }
                        )
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(12.dp)) }

                            itemsIndexed(state.messages) { index, msg ->
                                MessageItemContainer(index = index) {
                                    MessageBubble(message = msg)
                                }
                            }

                            // Dynamic token-by-token streaming chunk bubble
                            if (state.isStreaming) {
                                item {
                                    MessageItemContainer(index = state.messages.size) {
                                        AssistantStreamingBubble(chunk = state.streamingChunk)
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }

                    // Elastic Animated error snackbar
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -50 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        state.error?.let { err ->
                            ErrorSnackbar(
                                message = err,
                                onDismiss = { viewModel.clearError() },
                                onRetry = { viewModel.retryLastMessage() }
                            )
                        }
                    }
                }

                // Chat Input Deck
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.example.ui.theme.AppColors.bgPrimary)
                        .drawBehind {
                            drawLine(
                                color = com.example.ui.theme.AppColors.border,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(com.example.ui.theme.AppColors.bgInput)
                            .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.border, shape = RoundedCornerShape(32.dp))
                            .padding(start = 20.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Input text inside Row
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Напиши сообщение...",
                                    color = com.example.ui.theme.AppColors.textHint,
                                    fontSize = 14.sp
                                )
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                                    color = com.example.ui.theme.AppColors.textPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = SolidColor(com.example.ui.theme.AppColors.green),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Upward/Send Button
                        val sendScale = remember { Animatable(1f) }
                        val sendEnabled = inputText.isNotBlank() && !state.isStreaming
                        
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(sendScale.value)
                                .clip(CircleShape)
                                .background(
                                    brush = if (sendEnabled) {
                                        com.example.ui.theme.AppColors.userBubble
                                    } else {
                                        Brush.linearGradient(listOf(com.example.ui.theme.AppColors.border, com.example.ui.theme.AppColors.border))
                                    }
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (sendEnabled) Color.Transparent else com.example.ui.theme.AppColors.border,
                                    shape = CircleShape
                                )
                                .clickable(enabled = sendEnabled) {
                                    scope.launch {
                                        sendScale.animateTo(0.85f, spring(stiffness = Spring.StiffnessHigh))
                                        sendScale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                                    }
                                    if (inputText.isNotBlank() && !state.isStreaming) {
                                        viewModel.sendMessage(inputText)
                                        inputText = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Отправить",
                                tint = if (sendEnabled) com.example.ui.theme.AppColors.greenDeep else com.example.ui.theme.AppColors.textHint,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer {
                                        rotationZ = -45f
                                      }
                            )
                        }
                    }
                }
            }
        }

        // Left settings cabinet drawer overlay
        SettingsDrawer(
            isOpen = isDrawerOpen,
            onClose = { isDrawerOpen = false },
            activeMode = state.activeMode,
            onSelectMode = { mode ->
                viewModel.selectMode(mode)
                isDrawerOpen = false
            },
            onClearHistory = {
                viewModel.clearHistory()
                isDrawerOpen = false
            },
            onTriggerDonate = {
                viewModel.showDonationPromptNow()
                isDrawerOpen = false
            },
            onSimulateDays = { days ->
                viewModel.simulateDaysElapsed(days)
            },
            daysElapsed = state.daysElapsed,
            hasDonated = state.preferences.hasDonated,
            totalDonated = state.preferences.totalDonatedRub
        )

        val context = androidx.compose.ui.platform.LocalContext.current

        // Live checkout URL handler to launch payments redirection
        LaunchedEffect(state.checkoutUrl) {
            state.checkoutUrl?.let { url ->
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Could not open checkout URL", e)
                }
                viewModel.clearCheckoutUrl()
            }
        }

        // YooKassa Secure donation sheet overlay
        DonationBottomSheet(
            isVisible = state.showDonationPrompt,
            onDismiss = { viewModel.dismissDonationPrompt() },
            onAmountSelected = { amount ->
                viewModel.executeYooKassaDonation(amount)
            }
        )

        // YooKassa Secure Processing Loop Screen
        YooKassaCheckoutModal(
            isActive = state.isYooKassaPaying,
            amount = state.currentDonationAmount
        )

        // Successful Payment Celebration Overlay
        DonationSuccessOverlay(
            isVisible = state.showDonationSuccess,
            amount = state.currentDonationAmount,
            onDismiss = { viewModel.dismissDonationSuccess() }
        )
    }
}

/**
 * Empty Chat state showing lovely localized presets
 */
@Composable
fun EmptyChatPlaceholder(
    mode: ChatMode,
    onSendPreset: (String) -> Unit
) {
    val presets = when (mode) {
        ChatMode.STANDARD -> listOf(
            "Расскажи интересную случайную мысль 🌿",
            "Напиши короткую добрую притчу"
        )
        ChatMode.MEME -> listOf(
            "Что значит 'сигма дед инсайд'? Объясни 🤪",
            "Расскажи мемный анекдот на злобу дня"
        )
        ChatMode.PSYCHOLOGIST -> listOf(
            "Чувствую сильную тревогу из-за дедлайнов... 🌸",
            "Как мне бережнее относиться к себе?"
        )
        ChatMode.GUIDE_MASTER -> listOf(
            "Какая мета сейчас в играх? Расскажи о новинках 🎮",
            "Дай быстрый гайд по выживанию"
        )
        ChatMode.BUSINESS -> listOf(
            "Как правильно составить краткий SWOT-анализ? 💼",
            "Выдели пять законов высокой эффективности"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(com.example.ui.theme.AppColors.bgSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌿", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Начни разговор с ${mode.title}!",
            color = com.example.ui.theme.AppColors.textPrimary,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Выбери одну из готовых фраз ниже или отправь любой свой вопрос:",
            color = com.example.ui.theme.AppColors.textSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        presets.forEach { text ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(com.example.ui.theme.AppColors.bgSurface)
                    .border(width = 0.5.dp, color = com.example.ui.theme.AppColors.border, shape = RoundedCornerShape(12.dp))
                    .clickable { onSendPreset(text) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    color = com.example.ui.theme.AppColors.greenDark,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Message bubble with user/assistant visuals
 */
@Composable
fun MessageBubble(
    message: MessageEntity
) {
    val isUser = message.role == "user"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Don't take whole screen
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 24.dp else 0.dp,
                        topEnd = if (isUser) 0.dp else 24.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(
                    brush = if (isUser) {
                        com.example.ui.theme.AppColors.userBubble
                    } else {
                        Brush.linearGradient(listOf(com.example.ui.theme.AppColors.aiBubbleBg, com.example.ui.theme.AppColors.aiBubbleBg))
                    }
                )
                .border(
                    width = if (isUser) 0.dp else 0.5.dp,
                    color = if (isUser) Color.Transparent else com.example.ui.theme.AppColors.aiBubbleBorder,
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 24.dp else 0.dp,
                        topEnd = if (isUser) 0.dp else 24.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.content,
                color = if (isUser) com.example.ui.theme.AppColors.userBubbleText else com.example.ui.theme.AppColors.aiBubbleText,
                fontSize = 15.sp,
                fontWeight = if (isUser) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Streaming Composing Bubble showing animated terminal cursor
 */
@Composable
fun AssistantStreamingBubble(
    chunk: String
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 24.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(com.example.ui.theme.AppColors.aiBubbleBg)
                .border(
                    width = 0.5.dp,
                    color = com.example.ui.theme.AppColors.aiBubbleBorder,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 24.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // If chunk is empty show a lovely placeholder sequence
                val contentText = if (chunk.isEmpty()) "Осмысление запроса" else chunk
                Text(
                    text = contentText,
                    color = if (chunk.isEmpty()) com.example.ui.theme.AppColors.textSecondary else com.example.ui.theme.AppColors.aiBubbleText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                MatchaSSECursor(
                    modifier = Modifier
                        .size(width = 8.dp, height = 15.dp)
                        .padding(bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Error Snackbar container
 */
@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2D1818))
            .border(width = 0.5.dp, color = Color(0xFFEF4444), shape = RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = message,
                color = TextWhite,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Закрыть",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF5555))
                        .clickable { onRetry() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Повторить заново 🗘",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Message bounce transition container
 */
@Composable
fun MessageItemContainer(
    index: Int,
    content: @Composable () -> Unit
) {
    val animateVal = remember { Animatable(50f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Staggered entry animation: 80ms delay between messages
        delay((index * 40L).coerceAtMost(400L))
        launch {
            animateVal.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        }
    }

    Box(
        modifier = Modifier
            .alpha(alphaAnim.value)
            .graphicsLayer {
                translationY = animateVal.value
            }
    ) {
        content()
    }
}
