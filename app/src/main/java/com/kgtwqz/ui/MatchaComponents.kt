package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MatchaBorder
import com.example.ui.theme.MatchaCard
import com.example.ui.theme.MatchaDark
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.MatchaGreenSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 3D Button implementation matching the layered BoxShadow spec:
 * shadows: [
 *   BoxShadow(offset: Offset(0,-2), color: Color(0x40FFFFFF), blurRadius: 0, spreadRadius: 0),
 *   BoxShadow(offset: Offset(0,4), color: Color(0x40000000), blurRadius: 8),
 * ]
 */
@Composable
fun MatchaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MatchaGreenPrimary,
    accentColor: Color = MatchaGreenSecondary,
    textColor: Color = com.example.ui.theme.AppColors.greenDeep,
    cornerRadius: Dp = 16.dp
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    var isPressed by remember { mutableStateOf(false) }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    Box(
        modifier = modifier
            .scale(scale.value)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        scale.animateTo(0.95f, springSpec)
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                            scale.animateTo(1f, springSpec)
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .drawBehind {
                val r = cornerRadius.toPx()
                
                // 1. Bottom Dark Shadow (Offset 0,4, Blur 8 equivalent representation via solid offset)
                if (!isPressed) {
                    drawRoundRect(
                        color = Color(0x40000000),
                        topLeft = Offset(0f, 4.dp.toPx()),
                        size = this.size,
                        cornerRadius = CornerRadius(r, r)
                    )
                }

                // 2. White Top Overlay Bezel Highlight (Offset 0,-2, Spread 0)
                drawRoundRect(
                    color = Color(0x40FFFFFF),
                    topLeft = Offset(0f, -2.dp.toPx()),
                    size = this.size,
                    cornerRadius = CornerRadius(r, r)
                )
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = if (enabled) listOf(backgroundColor, accentColor) else listOf(MatchaCard, MatchaDark)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 0.5.dp,
                color = if (enabled) Color(0x20FFFFFF) else MatchaBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else TextGray,
            fontSize = 16.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            style = LocalTextStyle.current.copy(letterSpacing = 0.5.sp)
        )
    }
}

/**
 * Handcrafted Card with Matcha Styling: #161B22 background, 0.5dp #21262D border, 20-24px rounded corners
 */
@Composable
fun MatchaCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                // Soft black shadow (Blur 20, Black 0.15) represented via subtle transparent expansion
                drawRoundRect(
                    color = Color(0x26000000),
                    topLeft = Offset(-1.dp.toPx(), 2.dp.toPx()),
                    size = this.size.copy(width = this.size.width + 2.dp.toPx(), height = this.size.height + 4.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadius.toPx() + 2.dp.toPx(), cornerRadius.toPx() + 2.dp.toPx())
                )
            }
            .background(
                color = MatchaCard,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 0.5.dp,
                color = MatchaBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Handcrafted Text Field that animates scale on focus (1.00 -> 1.02) using spring physics
 */
@Composable
fun MatchaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    maxLines: Int = 4
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isFocused) {
        val targetScale = if (isFocused) 1.02f else 1.00f
        scaleAnim.animateTo(
            targetValue = targetScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scaleAnim.value)
            .background(
                color = com.example.ui.theme.AppColors.bgInput,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (isFocused) com.example.ui.theme.AppColors.green else com.example.ui.theme.AppColors.border,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = com.example.ui.theme.AppColors.textHint,
                fontSize = 15.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = LocalTextStyle.current.copy(
                color = com.example.ui.theme.AppColors.textPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(com.example.ui.theme.AppColors.green),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            maxLines = maxLines
        )
    }
}

/**
 * Pulsing/blinking green cursor rectangle representing streaming completions
 */
@Composable
fun MatchaSSECursor(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Canvas(modifier = modifier) {
        drawRect(
            color = MatchaGreenPrimary.copy(alpha = alpha),
            size = this.size
        )
    }
}

/**
 * Pure Kotlin Confetti/Floating Particles canvas animation for donation success screens
 */
@Composable
fun MatchaConfetti(
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    if (!isActive) return

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isActive) {
        particles.clear()
        // Generate initial explosion of 80 particles
        repeat(80) {
            particles.add(
                ConfettiParticle(
                    posX = Random.nextFloat(),
                    posY = 1.1f, // Launch from below
                    velX = (Random.nextFloat() - 0.5f) * 0.08f,
                    velY = -Random.nextFloat() * 0.06f - 0.04f,
                    color = listOf(MatchaGreenPrimary, MatchaGreenSecondary, Color.White, Color(0xFFFACC15)).random(),
                    size = Random.nextFloat() * 8f + 6f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (Random.nextFloat() - 0.5f) * 10f
                )
            )
        }

        // Particle physics render loop
        while (isActive) {
            delay(16) // ~60fps
            for (i in particles.indices) {
                val p = particles[i]
                val nextY = p.posY + p.velY
                val nextX = p.posX + p.velX
                // Add minor gravity pull and wind drift
                val nextVelY = p.velY + 0.0008f
                val nextRot = p.rotation + p.rotSpeed
                particles[i] = p.copy(
                    posX = nextX,
                    posY = nextY,
                    velY = nextVelY,
                    rotation = nextRot
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val px = p.posX * w
            val py = p.posY * h

            // Skip out-of-screen particles
            if (py in -100f..(h + 100f) && px in -100f..(w + 100f)) {
                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    // Draw square/rectangular confetti ribbons
                    drawRect(
                        color = p.color,
                        topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f)
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val posX: Float,
    val posY: Float,
    val velX: Float,
    val velY: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotSpeed: Float
)
