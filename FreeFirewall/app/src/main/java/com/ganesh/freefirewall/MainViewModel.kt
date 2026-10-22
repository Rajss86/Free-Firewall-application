package com.ganesh.freefirewall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RulesRepository(app)
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps
    private val _blocked = MutableStateFlow(repo.blockedPackages())
    val blocked: StateFlow<Set<String>> = _blocked
    init { _apps.value = AppScanner.load(app) }
    fun setBlocked(pkg: String, value: Boolean) { repo.setBlocked(pkg, value); _blocked.value = repo.blockedPackages() }
}
