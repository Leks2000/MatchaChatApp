package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.MatchaGreenSecondary

@Composable
fun MatchaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    
    // Smooth breathing steam animation
    val steamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steamPulse"
    )

    // Smooth subtle rotating logo glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // Draw Matcha glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MatchaGreenPrimary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = centerX * glowScale
                ),
                radius = centerX
            )

            // Draw tea cup outer outline
            val path = Path().apply {
                // Cup shape
                moveTo(centerX - w * 0.25f, centerY - h * 0.1f)
                lineTo(centerX + w * 0.25f, centerY - h * 0.1f)
                quadraticTo(
                    centerX + w * 0.25f, centerY + h * 0.25f,
                    centerX, centerY + h * 0.25f
                )
                quadraticTo(
                    centerX - w * 0.25f, centerY + h * 0.25f,
                    centerX - w * 0.25f, centerY - h * 0.1f
                )
            }

            // Draw Tea inside the cup (Green Matcha Fill!)
            val teaPath = Path().apply {
                moveTo(centerX - w * 0.23f, centerY - h * 0.05f)
                lineTo(centerX + w * 0.23f, centerY - h * 0.05f)
                quadraticTo(
                    centerX + w * 0.23f, centerY + h * 0.23f,
                    centerX, centerY + h * 0.23f
                )
                quadraticTo(
                    centerX - w * 0.23f, centerY + h * 0.23f,
                    centerX - w * 0.23f, centerY - h * 0.05f
                )
            }

            drawPath(
                path = teaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(MatchaGreenPrimary, MatchaGreenSecondary)
                )
            )

            // Draw cup handle
            val handlePath = Path().apply {
                moveTo(centerX + w * 0.24f, centerY - h * 0.02f)
                cubicTo(
                    centerX + w * 0.40f, centerY - h * 0.08f,
                    centerX + w * 0.40f, centerY + h * 0.18f,
                    centerX + w * 0.24f, centerY + h * 0.12f
                )
            }
            drawPath(
                path = handlePath,
                color = MatchaGreenSecondary,
                style = Stroke(width = 6f)
            )

            // Draw cup base line / plate
            drawRoundRect(
                color = MatchaGreenSecondary,
                topLeft = Offset(centerX - w * 0.35f, centerY + h * 0.27f),
                size = Size(w * 0.7f, 6.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Draw MatchaGreenSecondary cup border
            drawPath(
                path = path,
                color = MatchaGreenSecondary,
                style = Stroke(width = 6f)
            )

            // Draw organic rising steam loops (animated!)
            val steamColor = MatchaGreenPrimary.copy(alpha = 0.5f)
            val steamPath1 = Path().apply {
                moveTo(centerX - w * 0.1f, centerY - h * 0.20f)
                cubicTo(
                    centerX - w * 0.15f - steamOffset, centerY - h * 0.30f,
                    centerX - w * 0.05f + steamOffset, centerY - h * 0.38f,
                    centerX - w * 0.1f, centerY - h * 0.45f
                )
            }
            val steamPath2 = Path().apply {
                moveTo(centerX + w * 0.1f, centerY - h * 0.18f)
                cubicTo(
                    centerX + w * 0.05f - steamOffset, centerY - h * 0.28f,
                    centerX + w * 0.15f + steamOffset, centerY - h * 0.36f,
                    centerX + w * 0.1f, centerY - h * 0.43f
                )
            }

            drawPath(
                path = steamPath1,
                color = steamColor,
                style = Stroke(width = 4f)
            )
            drawPath(
                path = steamPath2,
                color = steamColor,
                style = Stroke(width = 4f)
            )
        }
    }
}
