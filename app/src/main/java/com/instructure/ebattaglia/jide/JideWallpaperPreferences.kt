package com.instructure.ebattaglia.jide

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log

class JideWallpaperPreferences(context: Context) {
    companion object {
        private const val DECK_ID = "deck_id"
        private const val LOCKSCREEN_FIRST_FIELD = "lockscreen_first_field"
        private const val LOCKSCREEN_SECOND_FIELD = "lockscreen_second_field"
        private const val LOCKSCREEN_LAUNCHER_SAME = "lockscreen_launcher_same"
        private const val LAUNCHER_FIRST_FIELD = "launcher_first_field"
        private const val LAUNCHER_SECOND_FIELD = "launcher_second_field"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun deckId() = prefs.getLong(DECK_ID, -1)
    fun lockscreenFirstField() = prefs.getString(LOCKSCREEN_FIRST_FIELD, "")
    fun lockscreenSecondField() = prefs.getString(LOCKSCREEN_SECOND_FIELD, "")
    fun lockscreenLauncherSame() = prefs.getBoolean(LOCKSCREEN_LAUNCHER_SAME, false)
    fun launcherFirstField() = prefs.getString(LAUNCHER_FIRST_FIELD, "")
    fun launcherSecondField() = prefs.getString(LAUNCHER_SECOND_FIELD, "")

    // TODO: this whole class is really verbose and boiilerplate...
    // should investigate better ways of doing this, such as using ViewModels and stuff
    fun set(deckId: Long, lockscreenFirstField: String, lockscreenSecondField: String,
        lockscreenLauncherSame: Boolean, launcherFirstField: String, launcherSecondField: String) {
        val edit = prefs.edit()
        edit.putLong(DECK_ID, deckId)
        edit.putString(LOCKSCREEN_FIRST_FIELD, lockscreenFirstField)
        edit.putString(LOCKSCREEN_SECOND_FIELD, lockscreenSecondField)
        edit.putBoolean(LOCKSCREEN_LAUNCHER_SAME, lockscreenLauncherSame)
        edit.putString(LAUNCHER_FIRST_FIELD, launcherFirstField)
        edit.putString(LAUNCHER_SECOND_FIELD, launcherSecondField)
        edit.apply()
    }

}