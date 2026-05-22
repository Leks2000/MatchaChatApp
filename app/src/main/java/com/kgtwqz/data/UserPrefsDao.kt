package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserPrefsDao {
    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getPrefsFlow(): kotlinx.coroutines.flow.Flow<UserPrefsEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 0")
    suspend fun getPrefs(): UserPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrefs(prefs: UserPrefsEntity)
}
