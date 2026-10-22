package com.ganesh.freefirewall

import android.content.Context

class RulesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("firewall_rules", Context.MODE_PRIVATE)
    fun blockedPackages(): Set<String> = prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()
    fun setBlocked(packageName: String, blocked: Boolean) {
        val rules = blockedPackages().toMutableSet()
        if (blocked) rules.add(packageName) else rules.remove(packageName)
        prefs.edit().putStringSet(KEY, rules).apply()
    }
    companion object { private const val KEY = "blocked_packages" }
}
