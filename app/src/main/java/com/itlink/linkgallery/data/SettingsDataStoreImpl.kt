package com.itlink.linkgallery.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStoreImpl @Inject constructor(private val context: Context) : SettingsDataStore {
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    override val isDarkMode: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[darkModeKey] ?: false
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[darkModeKey] = enabled }
    }
}
