package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (ChatMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = ChatMode.values()
    var selectedIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // We use a scrollable list of horizontal cards to behave exactly like an elegant pager
    val lazyListState = rememberLazyListState()

    // Sync lazyRow scroll index back to selectedIndex
    val visibleIndex = remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) 0
            else {
                val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                val closest = visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }
                closest?.index ?: 0
            }
        }
    }

    LaunchedEffect(visibleIndex.value) {
        if (visibleIndex.value < modes.size) {
            selectedIndex = visibleIndex.value
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatchaDark)
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Выбери характер",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = (-0.5).sp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Твой ИИ подстроится под твои задачи 🌿",
                    color = com.example.ui.theme.AppColors.greenDark,
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
                )
            }

            // Cards Carousel with Spring-swapping slide transition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(modes) { index, mode ->
                        val isSelected = index == selectedIndex
                        val cardScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 0.90f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "onboardCardScale"
                        )
                        val cardAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.5f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "onboardCardAlpha"
                        )

                        OnboardingModeCard(
                            mode = mode,
                            isSelected = isSelected,
                            modifier = Modifier
                                .width(280.dp)
                                .height(300.dp)
                                .scale(cardScale)
                                .alpha(cardAlpha)
                                .clickable {
                                    selectedIndex = index
                                    scope.launch {
                                        lazyListState.animateScrollToItem(index)
                                    }
                                }
                        )
                    }
                }
            }

            // System prompt preview using AnimatedSwitcher specification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = modes[selectedIndex],
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { width -> width },
                            animationSpec = spring()
                        ) + fadeIn(animationSpec = spring()) with
                        slideOutHorizontally(
                            targetOffsetX = { width -> -width },
                            animationSpec = spring()
                        ) + fadeOut(animationSpec = spring()) using SizeTransform(clip = false)
                    },
                    label = "modeSwitchSpec"
                ) { mode ->
                    Text(
                        text = mode.description,
                        color = TextGray,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Page Indicator dot matrix
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                modes.forEachIndexed { index, _ ->
                    val isSelected = index == selectedIndex
                    val dotWidth by animateFloatAsState(
                        targetValue = if (isSelected) 24f else 8f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = dotWidth.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MatchaGreenPrimary else TextMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Button to proceed
            MatchaButton(
                text = "Начать общение 🌿",
                onClick = {
                    onComplete(modes[selectedIndex])
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OnboardingModeCard(
    mode: ChatMode,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    // Elegant character icons
    val icon = when (mode) {
        ChatMode.STANDARD -> "🌿"
        ChatMode.MEME -> "😜"
        ChatMode.PSYCHOLOGIST -> "🌸"
        ChatMode.GUIDE_MASTER -> "🎮"
        ChatMode.BUSINESS -> "💼"
    }

    val glowBrush = Brush.sweepGradient(
        colors = listOf(MatchaGreenPrimary, MatchaGreenSecondary, MatchaGreenPrimary)
    )

    Box(
        modifier = modifier
            .background(MatchaCard, shape = RoundedCornerShape(24.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) glowBrush else SolidColor(MatchaBorder),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Giant Emoji Backdrop
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(com.example.ui.theme.AppColors.greenGlow, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = mode.title,
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val label = when (mode) {
                ChatMode.STANDARD -> "Повседневный"
                ChatMode.MEME -> "Рунет-постирония"
                ChatMode.PSYCHOLOGIST -> "Поддержка КПТ"
                ChatMode.GUIDE_MASTER -> "Игровой гид"
                ChatMode.BUSINESS -> "Бизнес-аналитика"
            }

            Box(
                modifier = Modifier
                    .background(Color(0x1A22C55E), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = com.example.ui.theme.AppColors.greenDark,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
