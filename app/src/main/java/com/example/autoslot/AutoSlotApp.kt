package com.example.autoslot

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/** Menerapkan preferensi tema (terang/gelap) dari SharedPreferences saat aplikasi dimulai. */
class AutoSlotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val mode = if (PrefsManager(this).darkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
