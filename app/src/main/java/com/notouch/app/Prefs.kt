package com.notouch.app

import android.content.Context

object Prefs {
    private const val PREFS_NAME = "no_touch_prefs"
    private const val KEY_PIN = "pin"
    private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PIN, "") ?: ""
    }

    fun setKeepScreenOn(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()
    }

    fun getKeepScreenOn(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_KEEP_SCREEN_ON, false)
    }
}
