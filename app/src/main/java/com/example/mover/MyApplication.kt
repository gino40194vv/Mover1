package com.example.mover

import android.app.Application
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Registriamo un callback per applicare i dynamic colors ad ogni Activity
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val prefs = activity.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    val colorMode = prefs.getInt("app_color", 0)
                    if (colorMode == 1) {
                        try {
                        } catch (e: Exception) {
                            Log.e("ThemeManager", "Error applying dynamic colors", e)
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        applyTheme()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        when (prefs.getInt("app_theme", 0)) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    companion object {
        private val DEFAULT_COLOR = Color.BLUE // Colore predefinito

        fun applyThemeToActivity(activity: Activity) {
            val prefs = activity.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val colorMode = prefs.getInt("app_color", 0)

            if (colorMode == 0) {  // Colori personalizzati dell'app
                activity.setTheme(R.style.Theme_Settings)
            } else if (colorMode == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivityIfAvailable(activity)
            }
            Log.d("ThemeDebug", "Applicato tema: ${if (colorMode == 0) "Theme_Settings" else "DynamicColors"}")

        }
    }
}
