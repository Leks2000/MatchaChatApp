package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.ChatMode
import com.example.data.ChatRepository
import com.example.data.MessageEntity
import com.example.data.UserPrefsEntity
import com.example.service.GroqLlmService
import com.example.service.LlmChunk
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val preferences: UserPrefsEntity = UserPrefsEntity(),
    val activeMode: ChatMode = ChatMode.STANDARD,
    val isStreaming: Boolean = false,
    val streamingChunk: String = "",
    val error: String? = null,
    val daysElapsed: Int = 0,
    val showDonationPrompt: Boolean = false,
    val showDonationSuccess: Boolean = false,
    val currentDonationAmount: Int = 0,
    val isYooKassaPaying: Boolean = false,
    val checkoutUrl: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(db.messageDao(), db.userPrefsDao())
    private val llmService = GroqLlmService()

    private val _isStreaming = MutableStateFlow(false)
    private val _streamingChunk = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)
    
    // Donation UI state
    private val _showDonationPrompt = MutableStateFlow(false)
    private val _showDonationSuccess = MutableStateFlow(false)
    private val _isYooKassaPaying = MutableStateFlow(false)
    private val _currentDonationAmount = MutableStateFlow(0)
    private val _checkoutUrl = MutableStateFlow<String?>(null)

    private var streamingJob: Job? = null
    private var lastSentMessage: String = "" // For retry purposes

    val preferences: StateFlow<UserPrefsEntity> = repository.prefsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPrefsEntity()
        )

    val activeMode: StateFlow<ChatMode> = preferences
        .combine(flowOf(ChatMode.values())) { prefs, modes ->
            modes.find { it.name == prefs.selectedChatMode } ?: ChatMode.STANDARD
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatMode.STANDARD
        )

    val messages: StateFlow<List<MessageEntity>> = activeMode
        .flatMapLatest { mode ->
            repository.getMessagesFlow(mode)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<ChatUiState> = combine(
        messages,
        preferences,
        activeMode,
        _isStreaming,
        _streamingChunk,
        _error,
        _showDonationPrompt,
        _showDonationSuccess,
        _isYooKassaPaying,
        _currentDonationAmount,
        _checkoutUrl
    ) { comb ->
        val msgs = comb[0] as List<MessageEntity>
        val prefs = comb[1] as UserPrefsEntity
        val mode = comb[2] as ChatMode
        val streaming = comb[3] as Boolean
        val chunk = comb[4] as String
        val err = comb[5] as String?
        val showPrompt = comb[6] as Boolean
        val showSuccess = comb[7] as Boolean
        val yookassaPaying = comb[8] as Boolean
        val valDonation = comb[9] as Int
        val url = comb[10] as String?

        val days = calculateDaysElapsed(prefs.firstLaunchDate)

        ChatUiState(
            messages = msgs,
            preferences = prefs,
            activeMode = mode,
            isStreaming = streaming,
            streamingChunk = chunk,
            error = err,
            daysElapsed = days,
            showDonationPrompt = showPrompt,
            showDonationSuccess = showSuccess,
            isYooKassaPaying = yookassaPaying,
            currentDonationAmount = valDonation,
            checkoutUrl = url
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        // Evaluate donation on app launch
        viewModelScope.launch {
            val prefs = repository.getPrefs()
            val days = calculateDaysElapsed(prefs.firstLaunchDate)
            
            // Donation schedule: Day 3, 6, 9, then every 15 days
            val shouldTrigger = days == 3 || days == 6 || days == 9 || (days > 9 && (days - 9) % 15 == 0)
            if (shouldTrigger && !prefs.hasDonated) {
                // Give a slight delay on launch before pushing bottom sheet
                delay(2000)
                _showDonationPrompt.value = true
            }
        }
    }

    private fun calculateDaysElapsed(firstLaunchMs: Long): Int {
        val elapsedMs = System.currentTimeMillis() - firstLaunchMs
        // Protect against negative/skewed dates and do integer math for elapsed days
        if (elapsedMs < 0) return 0
        return (elapsedMs / (1000L * 60 * 60 * 24)).toInt()
    }

    fun selectMode(mode: ChatMode) {
        viewModelScope.launch {
            repository.updateSelectedChatMode(mode)
        }
    }

    fun completeOnboarding(mode: ChatMode) {
        viewModelScope.launch {
            repository.completeOnboarding(mode)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _error.value = null
        lastSentMessage = text

        val mode = uiState.value.activeMode
        viewModelScope.launch {
            // Write User Message to DB
            repository.addMessage(mode, "user", text)
            
            // Execute Stream completion
            startCompletionStream(mode)
        }
    }

    fun retryLastMessage() {
        if (lastSentMessage.isBlank()) return
        _error.value = null
        val mode = uiState.value.activeMode
        viewModelScope.launch {
            startCompletionStream(mode)
        }
    }

    private fun startCompletionStream(mode: ChatMode) {
        streamingJob?.cancel()
        _isStreaming.value = true
        _streamingChunk.value = ""

        streamingJob = viewModelScope.launch {
            // Get rolling history window of 20 messages for prompt injection context
            val contextHistory = repository.getRecentMessagesForContext(mode, 20)
            val apiKey = BuildConfig.GROQ_API_KEY

            var combinedResponse = ""
            
            llmService.streamCompletions(mode.systemPrompt, contextHistory, apiKey).collect { chunk ->
                when (chunk) {
                    is LlmChunk.Content -> {
                        combinedResponse += chunk.text
                        _streamingChunk.value = combinedResponse
                    }
                    is LlmChunk.Error -> {
                        _error.value = chunk.message
                        _isStreaming.value = false
                        streamingJob?.cancel()
                    }
                    is LlmChunk.Done -> {
                        // Persist complete message
                        repository.addMessage(mode, "assistant", combinedResponse)
                        _streamingChunk.value = ""
                        _isStreaming.value = false
                    }
                }
            }
        }
    }

    fun clearHistory() {
        val mode = uiState.value.activeMode
        viewModelScope.launch {
            repository.clearHistory(mode)
        }
    }

    fun showDonationPromptNow() {
        _showDonationPrompt.value = true
    }

    fun dismissDonationPrompt() {
        _showDonationPrompt.value = false
    }

    fun dismissDonationSuccess() {
        _showDonationSuccess.value = false
        _currentDonationAmount.value = 0
    }

    fun executeYooKassaDonation(amount: Int) {
        _currentDonationAmount.value = amount
        _showDonationPrompt.value = false
        _isYooKassaPaying.value = true
        _error.value = null
        
        viewModelScope.launch {
            val userPrefs = repository.getPrefs()
            val userId = userPrefs.supabaseUserId
            
            // Map donation amounts to compatible subscription types in users table
            val type = when (amount) {
                49 -> "pro_monthly"
                99 -> "pro_annual"
                199 -> "pro_lifetime"
                else -> "pro_monthly"
            }
            
            // Try contacting real Supabase Edge Function checkout
            val confirmationUrl = com.example.service.SupabasePayService.getCheckoutUrl(userId, type)
            
            _isYooKassaPaying.value = false
            if (confirmationUrl != null) {
                _checkoutUrl.value = confirmationUrl
            } else {
                _error.value = "Запущен ознакомительный платеж. (Для реальной оплаты подключите Edge-функцию в Supabase)"
                // Fallback to high-fidelity payment simulation
                _isYooKassaPaying.value = true
                delay(3000)
                _isYooKassaPaying.value = false
                repository.markDonated(amount)
                _showDonationSuccess.value = true
            }
        }
    }

    fun clearCheckoutUrl() {
        _checkoutUrl.value = null
    }

    // Process returning back after successful YooKassa payment via deep link
    fun handlePaymentReturn() {
        _checkoutUrl.value = null
        val amount = _currentDonationAmount.value
        if (amount > 0) {
            viewModelScope.launch {
                repository.markDonated(amount)
                _showDonationSuccess.value = true
            }
        }
    }

    // Interactive developer settings mocks for Day 3, 6, 9 testing
    fun simulateDaysElapsed(days: Int) {
        viewModelScope.launch {
            repository.setMockFirstLaunchOffset(days)
            _error.value = "Симулировано дней: $days. Перезапустите страницу или переключите режим"
        }
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
