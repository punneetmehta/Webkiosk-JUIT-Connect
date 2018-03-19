package juit.webkiosk.application

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager

import com.onesignal.OSNotification
import com.onesignal.OSNotificationAction
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal

import org.json.JSONObject

import juit.webkiosk.R
import juit.webkiosk.database.NotificationsDB
import juit.webkiosk.helper.ConnectivityReceiver
import juit.webkiosk.model.Notification
import juit.webkiosk.preference.SettingsPref

/**
 * Created by puneet on 08/10/17.
 */

class JUITWebKiosk : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        val settingsPref = SettingsPref(this)
        NotiDB = NotificationsDB(this)
        // Check if Notification is Allowed
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(NotificationOpenedHandler(this))
                .setNotificationReceivedHandler(NotificationReceivedHandler(this))
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    private inner class NotificationReceivedHandler(private val application: Application) : OneSignal.NotificationReceivedHandler {
        internal var settingsPref: SettingsPref

        init {
            settingsPref = SettingsPref(application)
        }

        override fun notificationReceived(notification: OSNotification) {
            val notificationID = notification.payload.notificationID
            val title = notification.payload.title
            val body = notification.payload.body
            var launchUrl = notification.payload.launchURL

            if(launchUrl==null){
                launchUrl = "NA";
            }

            Log.e("OneSignal", "NotificationID received: " + notificationID)

            val noti = Notification()
            noti.id = notificationID;
            noti.title = title
            noti.message = body
            noti.link = launchUrl
            NotiDB!!.addNotification(noti)

            Log.e("OneSignal", "Notification Added to Database....")
        }
    }


    private inner class NotificationOpenedHandler(private val application: Application) : OneSignal.NotificationOpenedHandler {
        internal var settingsPref: SettingsPref

        init {
            settingsPref = SettingsPref(application)
        }

        override fun notificationOpened(result: OSNotificationOpenResult) {
            Log.e("OneSignalExample", "result : " + result.toString())
            val notificationID = result.notification.payload.notificationID
            val title = result.notification.payload.title
            val body = result.notification.payload.body
            var launchUrl = result.notification.payload.launchURL

            if(launchUrl==null){
                launchUrl = "";
            }
            Log.e("OneSignalExample", "title : " + title)
            Log.e("OneSignalExample", "body : " + body)
            Log.e("OneSignalExample", "launchUrl : " + launchUrl)

            startMain(notificationID, title, body, launchUrl)
        }

        fun startMain(notiID: String,title: String, message: String, launchURL: String) {
            val intent = Intent(application, juit.webkiosk.activities.Notification::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("id",notiID);
            intent.putExtra("title", title)
            intent.putExtra("message", message)
            intent.putExtra("launchURL", launchURL)
            application.startActivity(intent)

        }

    }

    companion object {

        @get:Synchronized
        var instance: JUITWebKiosk? = null
            private set
        var NotiDB: NotificationsDB? = null
    }
}
