package juit.webkiosk.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

import juit.webkiosk.application.JUITWebKiosk

/**
 * Created by puneet on 16/10/17.
 */

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnected)
        }
    }


    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {

        var connectivityReceiverListener: ConnectivityReceiverListener? = null

        val isConnected: Boolean
            get() {
                val cm = JUITWebKiosk.instance!!.applicationContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }
    }
}
