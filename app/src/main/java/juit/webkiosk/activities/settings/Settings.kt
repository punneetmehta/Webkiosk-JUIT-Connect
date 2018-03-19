package juit.webkiosk.activities.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast

import com.onesignal.OneSignal

import juit.webkiosk.R
import juit.webkiosk.application.JUITWebKiosk
import juit.webkiosk.database.UserDetailsDB
import juit.webkiosk.helper.ConnectivityReceiver
import juit.webkiosk.model.UserDetailsResponse
import juit.webkiosk.preference.LoginSessionPref
import juit.webkiosk.preference.TimeTablePref

/**
 * Created by puneet on 09/10/17.
 */

class Settings : AppCompatPreferenceActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInternetAvailable = ConnectivityReceiver.isConnected

        try {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: Exception) {
            try {
                actionBar!!.setDisplayHomeAsUpEnabled(true)
            } catch (e1: Exception) {

            }

        }

        // load settings fragment
        fragmentManager.beginTransaction().replace(android.R.id.content, MainPreferenceFragment()).commit()
    }

    class MainPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.settings)

            val username = LoginSessionPref(activity).username
            val userDetailsResponse = UserDetailsDB(activity).getDetails(username)
            val userbatch = TimeTablePref(activity).getBatch()
            try {
                findPreference("appVersion").summary = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
            } catch (e: Exception) {

            }

            val userEnrNo = findPreference("userEnrNo")
            userEnrNo.summary = LoginSessionPref(activity).username
            val userName = findPreference("userName")
            userName.summary = userDetailsResponse.name
            val userBatch = findPreference("userBatch")
            userBatch.summary = TimeTablePref(activity).getBatch()

            val allowNotifications = findPreference("allowNotifications")

            allowNotifications.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                isInternetAvailable = ConnectivityReceiver.isConnected
                if (isInternetAvailable!!) {
                    val value = newValue as Boolean
                    OneSignal.setSubscription(value)
                    allowNotifications.setDefaultValue(value)
                } else {
                    Toast.makeText(activity, "Internet Connectivity is Required...", Toast.LENGTH_SHORT).show()
                    return@OnPreferenceChangeListener false
                }
                true
            }


            val myPref = findPreference("feedback")
            myPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                sendFeedback(activity)
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        JUITWebKiosk.instance!!.setConnectivityListener(this)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        isInternetAvailable = isConnected
    }

    companion object {
        private val TAG = Settings::class.java.simpleName
        private var isInternetAvailable: Boolean? = false

        /**
         * Email client intent to send support mail
         * Appends the necessary device information to email body
         * useful when providing support
         */
        fun sendFeedback(context: Context) {
            var body: String? = null
            try {
                body = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                        Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                        "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER
            } catch (e: PackageManager.NameNotFoundException) {
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("dev.puneetmehta@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, LoginSessionPref(context).username + " : Feedback - JUIT Webkiosk")
            intent.putExtra(Intent.EXTRA_TEXT, body)
            context.startActivity(Intent.createChooser(intent, "Choose Email Client :"))
        }
    }

}