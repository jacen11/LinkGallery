package com.itlink.linkgallery.data

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isAvailable: Flow<Boolean>
}
