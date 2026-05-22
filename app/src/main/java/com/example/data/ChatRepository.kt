package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val messageDao: MessageDao,
    private val userPrefsDao: UserPrefsDao
) {
    // Collect preferences and map to safe default values
    val prefsFlow: Flow<UserPrefsEntity> = userPrefsDao.getPrefsFlow().map { prefs ->
        prefs ?: createDefaultPrefs()
    }

    suspend fun getPrefs(): UserPrefsEntity {
        return userPrefsDao.getPrefs() ?: createDefaultPrefs().also {
            userPrefsDao.insertPrefs(it)
        }
    }

    private suspend fun createDefaultPrefs(): UserPrefsEntity {
        val defaultPrefs = UserPrefsEntity(
            id = 0,
            selectedChatMode = "STANDARD",
            firstLaunchDate = System.currentTimeMillis(),
            hasDonated = false,
            totalDonatedRub = 0,
            isOnboardingCompleted = false
        )
        userPrefsDao.insertPrefs(defaultPrefs)
        return defaultPrefs
    }

    suspend fun completeOnboarding(mode: ChatMode) {
        val current = getPrefs()
        userPrefsDao.insertPrefs(
            current.copy(
                selectedChatMode = mode.name,
                isOnboardingCompleted = true
            )
        )
    }

    suspend fun setMockFirstLaunchOffset(daysAgo: Int) {
        val current = getPrefs()
        val offsetMs = daysAgo * 24L * 60 * 60 * 1000
        userPrefsDao.insertPrefs(
            current.copy(
                firstLaunchDate = System.currentTimeMillis() - offsetMs
            )
        )
    }

    suspend fun updateSelectedChatMode(mode: ChatMode) {
        val current = getPrefs()
        userPrefsDao.insertPrefs(current.copy(selectedChatMode = mode.name))
    }

    suspend fun markDonated(amount: Int) {
        val current = getPrefs()
        userPrefsDao.insertPrefs(
            current.copy(
                hasDonated = true,
                totalDonatedRub = current.totalDonatedRub + amount
            )
        )
    }

    fun getMessagesFlow(mode: ChatMode): Flow<List<MessageEntity>> {
        return messageDao.getMessagesByModeFlow(mode.name)
    }

    suspend fun getRecentMessagesForContext(mode: ChatMode, limit: Int = 20): List<MessageEntity> {
        // Returns last N messages, reversed/sorted chronologically
        val list = messageDao.getRecentMessages(mode.name, limit)
        return list.reversed()
    }

    suspend fun addMessage(mode: ChatMode, role: String, content: String) {
        val msg = MessageEntity(
            chatMode = mode.name,
            role = role,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(msg)
    }

    suspend fun clearHistory(mode: ChatMode) {
        messageDao.clearHistoryByMode(mode.name)
    }
}
