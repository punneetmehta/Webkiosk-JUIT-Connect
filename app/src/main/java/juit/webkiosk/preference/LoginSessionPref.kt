package juit.webkiosk.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE

/**
 * Created by puneet on 05/10/17.
 */

class LoginSessionPref(internal var mContext: Context) {
    internal var sharedPrefName = "Login"
    internal var loginSession_KEY = "loginSession_v2"
    internal var username_KEY = "loginUsername"
    internal var password_KEY = "loginPassword"

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor

    var sessionKey: String
        get() = pref.getString(loginSession_KEY, "")
        set(session) {
            editor.putString(loginSession_KEY, session)
            editor.apply()
        }
    var username: String
        get() = pref.getString(username_KEY, "")
        set(session) {
            editor.putString(username_KEY, session)
            editor.apply()
        }
    var password: String
        get() = pref.getString(password_KEY, "")
        set(session) {
            editor.putString(password_KEY, session)
            editor.apply()
        }

    init {
        pref = mContext.getSharedPreferences(sharedPrefName, MODE_PRIVATE)
        editor = pref.edit()
    }

}
