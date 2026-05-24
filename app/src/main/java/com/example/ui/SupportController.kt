package com.example.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val id: String,
    val amount: Int,
    val tierName: String,
    val date: String,
    val status: String, // "Успешно", "В обработке", "Отклонено"
    val method: String // "СБП", "Банковская карта", "Mir Pay"
)

object SupportManager {
    // Persistent-like in-memory store for simulation
    val transactions = mutableStateListOf<Transaction>(
        Transaction("yk_948194", 150, "Чашечка чая матча", "21.05.2026, 12:44", "Успешно", "СБП"),
        Transaction("yk_381048", 450, "Дзен-медитация", "23.05.2026, 18:15", "Успешно", "Банковская карта")
    )

    // Popup alternate status
    val showBannerPrompt = mutableStateOf(false)
    val currentPromptType = mutableStateOf(SuggestionType.RATE) // Alternates between RATE and DONATE
    val actionCounter = mutableStateOf(0)

    enum class SuggestionType {
        RATE, DONATE
    }

    // Call when user executes key actions to increment rate/donate chance
    fun recordAction() {
        actionCounter.value += 1
        if (actionCounter.value >= 4) { // Show popup every 4 key interactions
            actionCounter.value = 0
            // Alternate type dynamically
            currentPromptType.value = if (currentPromptType.value == SuggestionType.RATE) {
                SuggestionType.DONATE
            } else {
                SuggestionType.RATE
            }
            showBannerPrompt.value = true
        }
    }

    fun addTransaction(amount: Int, tierName: String, method: String) {
        val formatter = SimpleDateFormat("dd.02.2026, HH:mm", Locale.getDefault())
        val dateString = formatter.format(Date())
        val randId = "yk_" + (100000..999999).random().toString()
        transactions.add(0, Transaction(randId, amount, tierName, dateString, "Успешно", method))
    }
}
