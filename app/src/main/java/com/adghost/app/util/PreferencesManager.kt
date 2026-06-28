package com.adghost.app.util

import android.content.Context

object PreferencesManager {

    private const val PREFS_NAME = "adghost_prefs"
    private const val KEY_SITES = "saved_sites"

    data class SavedSite(val nickname: String, val url: String)

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSites(context: Context): List<SavedSite> {
        val set = getPrefs(context).getStringSet(KEY_SITES, emptySet()) ?: emptySet()
        return set.mapNotNull { line ->
            val parts = line.split("|||", limit = 2)
            if (parts.size == 2) SavedSite(parts[0], parts[1]) else null
        }
    }

    fun addSite(context: Context, nickname: String, url: String) {
        val prefs = getPrefs(context)
        val set = prefs.getStringSet(KEY_SITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add("$nickname|||$url")
        prefs.edit().putStringSet(KEY_SITES, set).apply()
    }

    fun removeSite(context: Context, nickname: String) {
        val prefs = getPrefs(context)
        val set = prefs.getStringSet(KEY_SITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.removeAll { it.startsWith("$nickname|||") }
        prefs.edit().putStringSet(KEY_SITES, set).apply()
    }

    fun updateSite(context: Context, oldNickname: String, newNickname: String, newUrl: String) {
        val prefs = getPrefs(context)
        val set = prefs.getStringSet(KEY_SITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.removeAll { it.startsWith("$oldNickname|||") }
        set.add("$newNickname|||$newUrl")
        prefs.edit().putStringSet(KEY_SITES, set).apply()
    }
}
