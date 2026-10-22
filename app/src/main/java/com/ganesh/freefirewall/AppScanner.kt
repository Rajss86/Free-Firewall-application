package com.ganesh.freefirewall

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

object AppScanner {
    fun load(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.applicationInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { ai -> AppInfo(pm.getApplicationLabel(ai).toString(), ai.packageName, (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0) }
            .sortedBy { it.label.lowercase() }
    }
}
