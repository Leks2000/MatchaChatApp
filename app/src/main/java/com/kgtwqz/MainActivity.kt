package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ChatScreen
import com.example.ui.OnboardingScreen
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var isSplashActive by remember { mutableStateOf(true) }
                val viewModel: ChatViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                // Catch returning deep link redirects from YooKassa
                androidx.compose.runtime.LaunchedEffect(intent?.data) {
                    intent?.data?.let { uri ->
                        if (uri.scheme == "matchachat" && uri.host == "payment-return") {
                            viewModel.handlePaymentReturn()
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Beautiful Crossfade navigation using custom tween timings to represent premium transitions
                    Crossfade(
                        targetState = isSplashActive,
                        animationSpec = tween(500),
                        label = "mainScreenTransition"
                    ) { splashActive ->
                        if (splashActive) {
                            SplashScreen(
                                isOnboardingCompleted = state.preferences.isOnboardingCompleted,
                                onSplashComplete = {
                                    isSplashActive = false
                                }
                            )
                        } else {
                            Crossfade(
                                targetState = state.preferences.isOnboardingCompleted,
                                animationSpec = tween(500),
                                label = "onboardTransition"
                              ) { onboardingCompleted ->
                                if (!onboardingCompleted) {
                                    OnboardingScreen(
                                        onComplete = { selectedMode ->
                                            viewModel.completeOnboarding(selectedMode)
                                        }
                                    )
                                } else {
                                    ChatScreen(
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            if (uri.scheme == "matchachat" && uri.host == "payment-return") {
                val viewModel = androidx.lifecycle.ViewModelProvider(this).get(ChatViewModel::class.java)
                viewModel.handlePaymentReturn()
            }
        }
    }
}
