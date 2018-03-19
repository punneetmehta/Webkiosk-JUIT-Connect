package juit.webkiosk.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by puneet on 09/10/17.
 */

class SettingsPref(internal var mContext: Context) {

    internal var notificationAllowed = "allowNotifications"
    internal var autoSyncAttendance = "autoUpdateAttendance"

    internal var prefs: SharedPreferences

    val isNotificationAllowed: Boolean?
        get() = prefs.getBoolean(notificationAllowed, true)

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    fun autoUpdateAttendance(): Boolean? {
        return prefs.getBoolean(autoSyncAttendance, true)
    }

}
