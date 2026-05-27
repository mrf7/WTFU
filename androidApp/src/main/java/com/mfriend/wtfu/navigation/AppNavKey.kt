package com.mfriend.wtfu.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface DeepLinkKey : NavKey {
    val parent: NavKey
}

@Serializable
data object AlarmList : NavKey

@Serializable
data class AlarmEdit(val id: Int? = null) : NavKey

/**
 * Deep-linkable alarm screen. [parent] is used to build a synthetic back stack so Back/Up
 * return to the list instead of exiting the app (see nav3-recipes deeplink guide).
 */
@Serializable
data class AlarmTrigger(val id: Int) : DeepLinkKey {
    override val parent: NavKey = AlarmList
}
