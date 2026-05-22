package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_preferences")
data class UserPrefsEntity(
    @PrimaryKey val id: Int = 0,
    val selectedChatMode: String = "STANDARD",
    val firstLaunchDate: Long = System.currentTimeMillis(),
    val hasDonated: Boolean = false,
    val totalDonatedRub: Int = 0,
    val isOnboardingCompleted: Boolean = false,
    val supabaseUserId: String = UUID.randomUUID().toString()
)
