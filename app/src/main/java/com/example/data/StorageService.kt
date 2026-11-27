package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.RetentionPolicy
import com.example.domain.model.SessionHistoryItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StorageService(context: Context? = null) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("safe_mvp_v1_prefs", Context.MODE_PRIVATE)

    inline fun <reified T> save(key: String, value: T) {
        val p = getPrefs() ?: return
        runCatching {
            val payload = json.encodeToString(value)
            p.edit().putString(key, payload).apply()
        }
    }

    inline fun <reified T> load(key: String, fallback: T): T {
        val p = getPrefs() ?: return fallback
        val raw = p.getString(key, null) ?: return fallback
        return runCatching { json.decodeFromString<T>(raw) }.getOrElse { fallback }
    }

    fun getPrefs(): SharedPreferences? = prefs

    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    fun applyHistoryRetention(
        items: List<SessionHistoryItem>,
        policy: RetentionPolicy,
        now: Long = System.currentTimeMillis(),
    ): List<SessionHistoryItem> {
        if (policy == RetentionPolicy.NONE) return emptyList()
        val maxAgeMs = when (policy) {
            RetentionPolicy.NONE -> 0L
            RetentionPolicy.HOURS_24 -> 24L * 60 * 60_000L
            RetentionPolicy.DAYS_7 -> 7L * 24 * 60 * 60_000L
            RetentionPolicy.DAYS_30 -> 30L * 24 * 60 * 60_000L
        }
        return items.filter { now - it.endedAt <= maxAgeMs }
    }
}
