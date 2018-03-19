package juit.webkiosk.preference

import android.content.Context
import android.content.SharedPreferences

import android.content.Context.MODE_PRIVATE
import android.os.Environment

/**
 * Created by puneet on 07/10/17.
 */

class TimeTablePref(internal var mContext: Context) {
    internal var sharedPrefName = "timeTable"
    internal var isTimeTableComputed = "timeTable_computed"
    internal var batch = "timetable_batch";

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor

    val timeTableComputed: Boolean?
        get() = pref.getBoolean(isTimeTableComputed, false)

    init {
        pref = mContext.getSharedPreferences(sharedPrefName, MODE_PRIVATE)
        editor = pref.edit()
    }

    fun setIsTimeTableComputed(session: Boolean?) {
        editor.putBoolean(isTimeTableComputed, session!!)
        editor.apply()
    }

    fun setBatch(b:String){
        editor.putString(batch, b!!)
        editor.apply()
    }

    fun getBatch():String{ return pref.getString(batch,"")};
    fun getTimeTableFile():String {
        return (Environment.getExternalStorageDirectory().toString()+"/JUIT_WebKiosk/")+getBatch()+".json";
    }
}
