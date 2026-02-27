package com.example.transportguide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    fun setAppLocale(context: android.content.Context, languageCode: String) {
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Сохраняем выбор
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit { putString("app_lang", languageCode) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 2. Загрузка темы
        val isDark = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark_mode", false)
        if (isDark) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val lang = getSharedPreferences("settings", MODE_PRIVATE).getString("app_lang", "ru")
        setAppLocale(this, lang ?: "ru")

        setContentView(R.layout.activity_main)
    }

}