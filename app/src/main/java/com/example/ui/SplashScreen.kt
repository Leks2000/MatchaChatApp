package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MatchaDark
import com.example.ui.theme.MatchaGreenPrimary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboardingCompleted: Boolean,
    onSplashComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Spring scaling and fading animations
    val logoScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo scale in with spring overshoot
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Fade in text slightly after logo starts springing
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
        )
        // Hold splash visible for 2500ms
        delay(2500)
        
        onSplashComplete(isOnboardingCompleted)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatchaDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            MatchaLogo(
                modifier = Modifier
                    .scale(logoScale.value),
                size = 140.dp
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {
                Text(
                    text = "Matcha Chat",
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(
                        letterSpacing = (-0.5).sp
                    )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Камерный ИИ-помощник",
                    color = com.example.ui.theme.AppColors.greenDark,
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "сделано для RuStore • 🌿",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                )
            }
        }
    }
}
