package juit.webkiosk.preference

import android.content.Context
import android.content.SharedPreferences

import android.content.Context.MODE_PRIVATE

/**
 * Created by puneet on 07/10/17.
 */

class firstTimeLaunch(internal var mContext: Context) {
    internal var sharedPrefName = "firstTimeLaunch"
    internal var mainActivity = "main"
    internal var loginActivity = "login"
    internal var attendanceActivity = "attendance"
    internal var detailAttendance = "detailAtt"
    internal var timeTableActivity = "timeTable"

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor


    var detailAttendanceSync: Boolean?
        get() = pref.getBoolean(detailAttendance, true)
        set(session) {
            editor.putBoolean(detailAttendance, session!!)
            editor.apply()
        }

    init {
        pref = mContext.getSharedPreferences(sharedPrefName, MODE_PRIVATE)
        editor = pref.edit()
    }

    fun setMainActivity(session: Boolean?) {
        editor.putBoolean(mainActivity, session!!)
        editor.apply()
    }

    fun setLoginActivity(session: Boolean?) {
        editor.putBoolean(loginActivity, session!!)
        editor.apply()
    }

    fun setAttendanceActivity(session: Boolean?) {
        editor.putBoolean(attendanceActivity, session!!)
        editor.apply()
    }

    fun setTimeTableActivity(session: Boolean?) {
        editor.putBoolean(timeTableActivity, session!!)
        editor.apply()
    }

    fun getMainActivity(): Boolean? {
        return pref.getBoolean(mainActivity, true)
    }

    fun getLoginActivity(): Boolean? {
        return pref.getBoolean(loginActivity, true)
    }

    fun getAttendanceActivity(): Boolean? {
        return pref.getBoolean(attendanceActivity, true)
    }

    fun getTimeTableActivity(): Boolean? {
        return pref.getBoolean(timeTableActivity, true)
    }

}
