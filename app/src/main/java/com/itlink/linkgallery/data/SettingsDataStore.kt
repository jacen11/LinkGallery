package com.itlink.linkgallery.data

import kotlinx.coroutines.flow.Flow

interface SettingsDataStore {
    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    val isOnboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
