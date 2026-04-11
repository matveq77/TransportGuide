package com.example.transportguide

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // Темная тема
        val sw = view.findViewById<SwitchCompat>(R.id.themeSwitch)
        sw.isChecked = prefs.getBoolean("dark_mode", false)
        sw.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 1. Выход из Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Переход на экран логина
            // Мы используем popUpTo в навигации, чтобы нельзя было вернуться назад
            findNavController().navigate(R.id.authFragment)
        }

        view.findViewById<Button>(R.id.btnLangRu).setOnClickListener {
            val mainAct = requireActivity() as MainActivity // Указываем, что это MainActivity
            mainAct.setAppLocale(requireContext(), "ru")    // Теперь функция будет видна
            mainAct.recreate()                              // Перезапуск для смены языка
        }

// Кнопка английского языка
        view.findViewById<Button>(R.id.btnLangEn).setOnClickListener {
            val mainAct = requireActivity() as MainActivity
            mainAct.setAppLocale(requireContext(), "en")
            mainAct.recreate()
        }
    }

    private fun changeLanguage(lang: String) {
        val activity = requireActivity() as MainActivity
        activity.setAppLocale(requireContext(), lang)
        activity.recreate() // Перезагружаем экран, чтобы язык применился
    }
}