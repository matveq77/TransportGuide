package com.example.transportguide

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.content.edit
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // Подавляем предупреждение о старом методе, так как для учебного проекта он самый надежный
    @Suppress("DEPRECATION")
    fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        // Обновляем конфигурацию
        resources.updateConfiguration(config, resources.displayMetrics)

        // Сохраняем выбор в SharedPreferences
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit { putString("app_lang", languageCode) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Splash Screen (ОБЯЗАТЕЛЬНО первым)
        installSplashScreen()

        // 2. Загрузка языка (ДО super.onCreate)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "ru") ?: "ru"
        setAppLocale(this, lang)

        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 3. Загрузка темы
        val isDark = prefs.getBoolean("dark_mode", false)
        if (isDark) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 4. Создание канала уведомлений (Исправлено для API < 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "transport_channel"
            val channelName = "Transport"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        setContentView(R.layout.activity_main)
    }
}