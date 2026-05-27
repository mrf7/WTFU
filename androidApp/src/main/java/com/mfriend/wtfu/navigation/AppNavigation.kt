package com.mfriend.wtfu.navigation

import android.content.Intent
import androidx.navigation3.runtime.NavKey

private const val DEEP_LINK_SCHEME = "https"
private const val DEEP_LINK_HOST = "mrfiend.com"

/**
 * Walks the [DeepLinkKey.parent] chain and builds a stack that simulates manual navigation
 * from the root destination (Principle 3 in the nav3-recipes deeplink guide).
 */
fun buildSyntheticBackStack(deepLinkKey: DeepLinkKey): List<NavKey> {
    val keys = mutableListOf<NavKey>()
    var current: NavKey? = deepLinkKey
    while (current != null) {
        keys.add(0, current)
        current = (current as? DeepLinkKey)?.parent
    }
    return keys
}

/**
 * Parses an incoming [Intent] URI into a [NavKey].
 *
 * Nav3 1.1.x has no built-in deep link matcher; this follows the nav3-recipes guide flow
 * (parse request → match pattern → decode to key) for `https://mrfiend.com/{id}`.
 */
fun intentToNavKey(intent: Intent?): NavKey {
    val uri = intent?.data ?: return AlarmList
    if (uri.scheme != DEEP_LINK_SCHEME || uri.host != DEEP_LINK_HOST) return AlarmList
    val id = uri.lastPathSegment?.toIntOrNull() ?: return AlarmList
    return AlarmTrigger(id)
}

/**
 * Initial back stack for [android.app.Activity.onCreate].
 *
 * - Launcher / no match → [AlarmList]
 * - Deep link (cold start) → synthetic stack simulating list → trigger (Principle 3)
 */
fun resolveStartKeys(intent: Intent?): List<NavKey> {
    val key = intentToNavKey(intent)
    if (key is DeepLinkKey) return buildSyntheticBackStack(key)
    return listOf(AlarmList)
}

/**
 * Key to push when [android.app.Activity.onNewIntent] delivers a deep link while the activity
 * is already on the task stack (existing task — add the destination, do not replace the stack).
 */
fun resolveDeepLinkForExistingTask(intent: Intent): NavKey? {
    val key = intentToNavKey(intent)
    return key.takeIf { it is DeepLinkKey }
}
