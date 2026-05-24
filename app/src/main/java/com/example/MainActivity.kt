package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.Content
import com.example.api.GeminiRepository
import com.example.api.Part
import com.example.ui.SupportManager
import com.example.ui.Transaction
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    containerColor = MintLight
                ) { innerPadding ->
                    MainDashboard(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class DashboardTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    COACH("Коуч AI", Icons.Default.Face),
    ZEN("Дзен Практика", Icons.Default.PlayArrow),
    PAYMENTS("ЮKassa Донат", Icons.Default.Favorite)
}

@Composable
fun MainDashboard(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(DashboardTab.COACH) }
    val coroutineScope = rememberCoroutineScope()

    // Interactive notification banner
    var showReviewNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }

    // YooKassa Checkout Session Data
    var isYooKassaOpen by remember { mutableStateOf(false) }
    var yooKassaAmount by remember { mutableStateOf(150) }
    var yooKassaTier by remember { mutableStateOf("Чашечка чая матча") }

    Column(
        modifier = modifier
            .background(MintLight)
            .fillMaxSize()
    ) {
        // App Header
        AppHeader(
            onOpenDonate = {
                yooKassaAmount = 450
                yooKassaTier = "Поддержка Матча Мастера"
                isYooKassaOpen = true
            }
        )

        // Rating/Support Overlay Banner triggered from action counts
        AnimatedVisibility(
            visible = SupportManager.showBannerPrompt.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            RatingSupportBanner(
                promptType = SupportManager.currentPromptType.value,
                onDismiss = { SupportManager.showBannerPrompt.value = false },
                onRate = { stars, comment ->
                    SupportManager.showBannerPrompt.value = false
                    notificationMessage = "Спасибо за вашу оценку в $stars звезд! ✨ Ваша поддержка бесценна!"
                    showReviewNotification = true
                },
                onSupport = {
                    SupportManager.showBannerPrompt.value = false
                    yooKassaAmount = 450
                    yooKassaTier = "Поддержка от Дзен-Друга"
                    isYooKassaOpen = true
                }
            )
        }

        // Active notification Toast-style feedback
        AnimatedVisibility(visible = showReviewNotification) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MintCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Успешно",
                        tint = DarkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = notificationMessage,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showReviewNotification = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            LaunchedEffect(showReviewNotification) {
                if (showReviewNotification) {
                    delay(5000)
                    showReviewNotification = false
                }
            }
        }

        // Feature Screen display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                DashboardTab.COACH -> CoachTabScreen()
                DashboardTab.ZEN -> ZenTabScreen()
                DashboardTab.PAYMENTS -> SupportTabScreen(
                    onInitiatePayment = { amount, tier ->
                        yooKassaAmount = amount
                        yooKassaTier = tier
                        isYooKassaOpen = true
                    }
                )
            }
        }

        // Bottom Navigation Tabs (No rounded background wraps on icons, fully flat in accordance with intent!)
        Divider(color = BorderColor, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val itemColor = if (isSelected) DarkGreen else MutedText
                val weight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Column(
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        color = itemColor,
                        fontWeight = weight
                    )
                }
            }
        }
    }

    // YooKassa Payment Overlay BottomSheet/Dialog
    if (isYooKassaOpen) {
        YooKassaCheckoutDialog(
            amount = yooKassaAmount,
            tierName = yooKassaTier,
            onDismiss = { isYooKassaOpen = false },
            onPaymentDone = { method ->
                isYooKassaOpen = false
                SupportManager.addTransaction(yooKassaAmount, yooKassaTier, method)
                notificationMessage = "Оплата $yooKassaAmount ₽ чая и подписки проведена успешно через ЮKassa! 🌱 Спасибо!"
                showReviewNotification = true
                SupportManager.recordAction() // Record action to alternate dialog trigger count
            }
        )
    }
}

@Composable
fun AppHeader(onOpenDonate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Logo",
                tint = DarkGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "🍵 Матча Мастер",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Ваш Велнес-Коуч",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
        }

        // Donation CTA button in the header
        Button(
            onClick = onOpenDonate,
            colors = ButtonDefaults.buttonColors(containerColor = MintCard),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Донат",
                tint = DarkGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Поддержать",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
        }
    }
    Divider(color = BorderColor, thickness = 1.dp)
}

// ----------------------------------------------------
// SCREEN tab: COACH (AI Companion Chats)
// ----------------------------------------------------
data class ChatMessage(val text: String, val isFromUser: Boolean, val timestamp: String)

@Composable
fun CoachTabScreen() {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Chat states
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("Приветствую тебя, искатель гармонии! 🍵 Я Матча Мастер, твой спокойный велнес-коуч. Как проходит твой сегодняшний день? Расскажи мне, или выбери одну из тем ниже, чтобы начать практику.", false, "12:00")
        )
    }
    var userTextState by remember { mutableStateOf("") }
    var isAILoading by remember { mutableStateOf(false) }

    val suggestionChips = listOf(
        "🧘 Спокойное Дыхание",
        "🍵 Рецепт Матча Колатте",
        "💤 Борьба со Стрессом",
        "🌿 Как проснуться утром?"
    )

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return
        chatMessages.add(ChatMessage(messageText, true, "Только что"))
        userTextState = ""
        isAILoading = true
        focusManager.clearFocus()

        // Count action for periodic popup trigger check
        SupportManager.recordAction()

        coroutineScope.launch {
            // Build simple conversation context mapping
            val historyContents = chatMessages.take(chatMessages.size - 1).map {
                Content(parts = listOf(Part(text = it.text)))
            }
            val botResponse = GeminiRepository.generateResponse(messageText, historyContents)
            chatMessages.add(ChatMessage(botResponse, false, "Только что"))
            isAILoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Conversation List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = false
        ) {
            items(chatMessages) { msg ->
                ChatBubble(msg)
            }
            if (isAILoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = DarkGreen,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Матча заваривается... Думает...",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prompt Chips Panel
        Text(
            text = "Быстрые темы для осознанности:",
            fontSize = 11.sp,
            color = MutedText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestionChips.forEach { chip ->
                Box(
                    modifier = Modifier
                        .background(MintCard, shape = RoundedCornerShape(12.dp))
                        .clickable { sendMessage(chip) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chip,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TextInput layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, shape = RoundedCornerShape(24.dp))
                .border(1.dp, BorderColor, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userTextState,
                onValueChange = { userTextState = it },
                placeholder = { Text("Задайте вопрос о матча или велнесе...", fontSize = 13.sp, color = MutedText) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            IconButton(
                onClick = { sendMessage(userTextState) },
                enabled = userTextState.isNotBlank() && !isAILoading,
                modifier = Modifier.testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Отправить",
                    tint = if (userTextState.isNotBlank()) DarkGreen else MutedText,
                    modifier = Modifier.size(20.dp) // Removed outline icon surround! Flat!
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val alignment = if (msg.isFromUser) Alignment.End else Alignment.Start
    val bubbleBg = if (msg.isFromUser) GreenAccent else White
    val textColor = if (msg.isFromUser) DeepGreen else TextPrimary
    val shapeRadius = if (msg.isFromUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleBg),
            shape = shapeRadius,
            border = if (!msg.isFromUser) BorderStroke(1.dp, BorderColor) else null,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.text,
                    fontSize = 14.sp,
                    color = textColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.timestamp,
                    fontSize = 9.sp,
                    color = if (msg.isFromUser) DeepGreen.copy(alpha = 0.6f) else MutedText,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN tab: ZEN (Breathing circle & Matcha timer)
// ----------------------------------------------------
@Composable
fun ZenTabScreen() {
    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Вдохните покой") }
    var breathingCount by remember { mutableStateOf(4) }
    var breathingAnimScale by remember { mutableStateOf(1f) }

    // Whisking Timer states
    var isTimerWhiskingActive by remember { mutableStateOf(false) }
    var timerProgress by remember { mutableStateOf(30) }
    var matchesWaterTemp by remember { mutableStateOf("80°C") }
    var matchesStyle by remember { mutableStateOf("Classic Usucha") }

    // Breathing Coroutine logic
    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            while (isBreathingActive) {
                // Inhale Phase (4 sec)
                breathingPhase = "🧘 ВДОХ"
                breathingAnimScale = 1.3f
                for (i in 4 downTo 1) {
                    breathingCount = i
                    delay(1000)
                }

                // Hold Phase (4 sec)
                breathingPhase = "🍃 ЗАДЕРЖКА ДЫХАНИЯ"
                for (i in 4 downTo 1) {
                    breathingCount = i
                    delay(1000)
                }

                // Exhale Phase (4 sec)
                breathingPhase = "🍃 ВЫДОХ"
                breathingAnimScale = 0.9f
                for (i in 4 downTo 1) {
                    breathingCount = i
                    delay(1000)
                }

                // Hold Out Phase (4 sec)
                breathingPhase = "✨ ПАУЗА"
                breathingAnimScale = 1f
                for (i in 4 downTo 1) {
                    breathingCount = i
                    delay(1000)
                }
            }
        } else {
            breathingPhase = "Нажмите для начала велнес-практики"
            breathingCount = 4
            breathingAnimScale = 1.0f
        }
    }

    // Tea Whisking Timer Logic
    LaunchedEffect(isTimerWhiskingActive) {
        if (isTimerWhiskingActive) {
            timerProgress = 30
            while (timerProgress > 0 && isTimerWhiskingActive) {
                delay(1000)
                timerProgress -= 1
            }
            isTimerWhiskingActive = false
            SupportManager.recordAction() // Record action to trigger rating periodic checks
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Breathing Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧘 Велнес Дыхание 4-4-4-4",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Квадратное дыхание убирает тревожность и охлаждает разум за минуты.",
                        fontSize = 12.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulated pulsing breathing container with simple responsive scale
                    val animatedSize by animateFloatAsState(targetValue = if (isBreathingActive) breathingAnimScale else 1f)
                    Box(
                        modifier = Modifier
                            .size((120 * animatedSize).dp)
                            .background(MintCard, shape = RoundedCornerShape(100.dp))
                            .border(2.dp, GreenAccent, shape = RoundedCornerShape(100.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = breathingCount.toString(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepGreen
                            )
                            Text(
                                text = "секунд",
                                fontSize = 11.sp,
                                color = DarkGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = breathingPhase,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { isBreathingActive = !isBreathingActive },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBreathingActive) AlertBg else DarkGreen,
                            contentColor = if (isBreathingActive) AlertText else White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("breathing_start_button")
                    ) {
                        Text(
                            text = if (isBreathingActive) "Остановить" else "Запустить практику",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tea Whisking Tea helper Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🍵 Таймер Мастера Матча",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Используйте для заваривания воды и идеального взбивания пенки.",
                        fontSize = 12.sp,
                        color = MutedText,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Температура воды:", fontSize = 11.sp, color = MutedText)
                            Row {
                                listOf("75°C", "80°C", "85°C").forEach { temp ->
                                    val sel = matchesWaterTemp == temp
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 4.dp, top = 4.dp)
                                            .background(if (sel) MintCard else LightGray, shape = RoundedCornerShape(8.dp))
                                            .clickable { matchesWaterTemp = temp }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(temp, fontSize = 11.sp, color = if (sel) DeepGreen else TextPrimary)
                                    }
                                }
                            }
                        }

                        Column {
                            Text("Стиль взбивания:", fontSize = 11.sp, color = MutedText)
                            Row {
                                listOf("Usucha", "Koicha").forEach { style ->
                                    val sel = matchesStyle == style
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 4.dp, top = 4.dp)
                                            .background(if (sel) MintCard else LightGray, shape = RoundedCornerShape(8.dp))
                                            .clickable { matchesStyle = style }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(style, fontSize = 11.sp, color = if (sel) DeepGreen else TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timer Whisk Tracker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightGray, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTimerWhiskingActive) "Взбивайте интенсивно W-образными движениями! 🍃" else "Таймер готов к работе",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Оставшееся время: $timerProgress сек",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = timerProgress / 30f,
                                color = GreenAccent,
                                trackColor = BorderColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { isTimerWhiskingActive = !isTimerWhiskingActive }
                        ) {
                            Icon(
                                imageVector = if (isTimerWhiskingActive) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Контроль",
                                tint = DarkGreen,
                                modifier = Modifier.size(28.dp) // Removed outline backdrop
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN tab: PAYMENTS (YooKassa integration)
// ----------------------------------------------------
@Composable
fun SupportTabScreen(onInitiatePayment: (Int, String) -> Unit) {
    val donationTiers = listOf(
        Pair(150, "Чашечка матча латте бариста 🍵"),
        Pair(450, "Набор дзен-благовоний и свечей 🧘"),
        Pair(900, "Премиум пачка органической матчи церемониального уровня ✨")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory premium/donation banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💳 Поддержка чатом через ЮKassa",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Мы интегрировали официальный терминал симуляции платежей ЮKassa! Вы можете внести вклад, симулирую подписки и транзакции.",
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Tier selection cards
        item {
            Text(
                text = "Выберите уровень поддержки:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(donationTiers) { tier ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MintCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInitiatePayment(tier.first, tier.second) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tier.second,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepGreen
                        )
                        Text(
                            text = "ЮKassa быстрый платёж",
                            fontSize = 11.sp,
                            color = DarkGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(White, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${tier.first} ₽",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Transaction History List
        item {
            Text(
                text = "История переводов ЮKassa:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (SupportManager.transactions.isEmpty()) {
            item {
                Text(
                    text = "Ещё не было совершено транзакций.",
                    fontSize = 12.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(SupportManager.transactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tx.tierName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${tx.id} • ${tx.method}",
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SuccessBg, shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tx.status,
                                        fontSize = 8.sp,
                                        color = DeepGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "+ ${tx.amount} ₽",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// UI BANNER: Smart Alternating Review / Donate suggestion
// ----------------------------------------------------
@Composable
fun RatingSupportBanner(
    promptType: SupportManager.SuggestionType,
    onDismiss: () -> Unit,
    onRate: (Int, String) -> Unit,
    onSupport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, DarkGreen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (promptType == SupportManager.SuggestionType.RATE) Icons.Default.Star else Icons.Default.Favorite,
                        contentDescription = "Алерт",
                        tint = DarkGreen,
                        modifier = Modifier.size(18.dp) // Flat design (Removed background box!)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (promptType == SupportManager.SuggestionType.RATE) "Как вам проект?" else "Поддержка Развития 🍃",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = MutedText, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (promptType == SupportManager.SuggestionType.RATE) {
                // RATING Flow
                var ratingSelection by remember { mutableStateOf(5) }
                var feedbackText by remember { mutableStateOf("") }

                Text(
                    text = "Помогите нам улучить велнес-коуча! Оцените проект звездами:",
                    fontSize = 11.sp,
                    color = MutedText
                )

                // Five stars layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    for (i in 1..5) {
                        val isSelected = i <= ratingSelection
                        IconButton(
                            onClick = { ratingSelection = i },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i Stars",
                                tint = if (isSelected) GreenAccent else BorderColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                TextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("Пара теплых слов (по желанию)...", fontSize = 11.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightGray,
                        unfocusedContainerColor = LightGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onRate(ratingSelection, feedbackText) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Оценить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

            } else {
                // DONATE Flow
                Text(
                    text = "Хотите поддержать проект? Мы развиваемся на донаты дзен-сообщества. Будем бесконечно рады паре рублей через ЮKassa!",
                    fontSize = 12.sp,
                    color = MutedText,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Позже", color = MutedText, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Оплатить в ЮKassa 🍵", color = DeepGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// POPUP SCREEN: High Fidelity YooKassa Checkout Terminal
// ----------------------------------------------------
@Composable
fun YooKassaCheckoutDialog(
    amount: Int,
    tierName: String,
    onDismiss: () -> Unit,
    onPaymentDone: (String) -> Unit
) {
    var checkoutStep by remember { mutableStateOf(1) } // 1: Select payment method, 2: Enter credentials, 3: Processing, 4: Done
    var selectedMethod by remember { mutableStateOf("Банковская карта") }

    // Card inputs with error validating
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvc by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // YooKassa Terminal header banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ю Kassa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6B4EE6) // Famous YooKassa Purple color token!
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFEECC), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Безопасный эквайринг", fontSize = 9.sp, color = Color(0xFFE58000), fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = BorderColor, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGray, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = tierName, fontSize = 12.sp, color = MutedText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(text = "Итого к оплате:", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(text = "$amount ₽", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (checkoutStep) {
                1 -> {
                    // STEP 1: Method selection
                    Text(text = "Выберите метод оплаты:", fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    val methodsList = listOf(
                        Pair("Банковская карта", Icons.Default.ShoppingCart),
                        Pair("СБП (Система Быстрых Платежей)", Icons.Default.Refresh),
                        Pair("Mir Pay", Icons.Default.Star)
                    )

                    methodsList.forEach { pair ->
                        val isSelected = selectedMethod == pair.first
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MintCard else White),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) DarkGreen else BorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedMethod = pair.first }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                Alignment.CenterVertically
                            ) {
                                Icon(pair.second, contentDescription = null, tint = if (isSelected) DarkGreen else MutedText, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(pair.first, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 6.dp)) {
                            Text("Отмена", color = MutedText)
                        }

                        Button(
                            onClick = {
                                if (selectedMethod == "Банковская карта") {
                                    checkoutStep = 2 // Proceed to Card inputs
                                } else {
                                    // SBP/Mir pay is instant
                                    checkoutStep = 3
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Продолжить", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    // STEP 2: Credit Card inputs
                    Text(text = "Введите реквизиты карты:", fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Error output
                    if (inputError.isNotBlank()) {
                        Text(
                            text = inputError,
                            color = AlertText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Card ID Input
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it },
                        label = { Text("Номер карты (16 цифр)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                            label = { Text("Срок (ММ/ГГ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        )

                        OutlinedTextField(
                            value = cardCvc,
                            onValueChange = { if (it.length <= 3) cardCvc = it },
                            label = { Text("CVC") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { checkoutStep = 1 }) {
                            Text("Назад", color = MutedText)
                        }

                        Button(
                            onClick = {
                                if (cardNumber.length < 16) {
                                    inputError = "Ошибка: Номер карты должен состоять из 16 цифр"
                                } else if (cardExpiry.length < 5 || !cardExpiry.contains("/")) {
                                    inputError = "Ошибка: Срок действия указан неверно (ММ/ГГ)"
                                } else if (cardCvc.length < 3) {
                                    inputError = "Ошибка: Неверный CVC-код"
                                } else {
                                    inputError = ""
                                    checkoutStep = 3 // Success processing
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Оплатить $amount ₽", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // STEP 3: Authentic YooKassa loaders
                    var connectionStatus by remember { mutableStateOf("Создание защищенного соединения HTTPS...") }

                    LaunchedEffect(Unit) {
                        delay(1200)
                        connectionStatus = "Отправка запроса авторизации в банк эмитент..."
                        delay(1200)
                        connectionStatus = "Подтверждение ЮKassa 3D-Secure..."
                        delay(1200)
                        onPaymentDone(selectedMethod)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = DarkGreen, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = connectionStatus,
                            fontSize = 12.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
