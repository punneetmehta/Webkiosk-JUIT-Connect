package juit.webkiosk.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import juit.webkiosk.R
import android.view.LayoutInflater
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import com.thefinestartist.utils.service.ClipboardManagerUtil.setText
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.thefinestartist.finestwebview.FinestWebView
import juit.webkiosk.database.NotificationsDB
import juit.webkiosk.model.Notification

/**
 * Created by puneet on 18/10/17.
 */

class Notification : AppCompatActivity() {

    lateinit var notiList : List<Notification>;
    lateinit var adapter : NotiAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        try {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: Exception) {
            try {
                actionBar!!.setDisplayHomeAsUpEnabled(true)
            } catch (e1: Exception) { }
        }

        try {
            val i : Intent = getIntent();
            handleNotificationIntent(i);
        } catch (e : Exception){ }

        val notiDB = NotificationsDB(this)

        Log.e("Notification","unreadNotificationCount() : "+notiDB.unreadNotificationCount())
        Log.e("Notification","notificationsCount() : "+notiDB.notificationsCount())

        notiList = notiDB.allNotifications.reversed()

        val listView = findViewById<ListView>(R.id.list_view)
        adapter = NotiAdapter(this,notiList)

        listView.setOnItemClickListener { parent, view, position, id ->

            val noti : Notification = notiList.get(position);

            val title : String  = noti.title as String;

            val message : String = noti.message as String;
            var link : String;

            if(noti.link.isNullOrEmpty()){
                link = "NA";
            } else {
                link = noti.link as String;
            }

            val notiID : String = noti.id as String;
            notiDB.markNotificationAsRead(noti);

            handleNotificationIntent(title,message,link);

        }
        listView.adapter = adapter
    }

    fun refreshNotiList(){
        notiList = NotificationsDB(this).allNotifications.reversed()
        adapter.notiList = notiList
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshNotiList()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        refreshNotiList()
        handleNotificationIntent(intent)
    }

    fun handleNotificationIntent(i: Intent) {
        try {
            val extras = i.extras
            val id = extras.getString("id");
            var tempNotiDB : NotificationsDB = NotificationsDB(this);
            tempNotiDB.markNotificationAsRead(tempNotiDB.getNotification(id));
            val title = extras.getString("title")
            var message : String? = ""
            try {
                message = extras.getString("message")
            } catch (e2:Exception){}

            var launchURL: String? = ""
            try {
                launchURL = extras.getString("launchURL")
                if(launchURL.equals("NA") || launchURL.isNullOrBlank() || launchURL.isNullOrEmpty()){
                    Log.e("Attendance_Notification", "No launchURL")
                    showDialog(title, message)
                } else {
                    showDialog(title, message, launchURL)
                }
            } catch (e: Exception) {
                Log.e("Attendance_Notification", "No launchURL")
                showDialog(title, message)
            }

        } catch (e: Exception) {
            Log.e("Attendance_Notification", "Unable to Get Notification Intent")
        }

    }

    fun handleNotificationIntent(title: String?, message: String?, launchURL: String?) {
            if(launchURL.equals("NA")){
                Log.e("Attendance_Notification", "No launchURL")
                showDialog(title, message)
            } else {
                    showDialog(title, message, launchURL)
            }

    }

    fun showDialog(title: String?, message: String?) {
        val alertDialog = AlertDialog.Builder(this@Notification).create()
        alertDialog.setTitle(title)
        if(!message.isNullOrBlank() || !message.isNullOrEmpty()) {
            alertDialog.setMessage(message)
        }
        alertDialog.setIcon(R.drawable.ic_stat_onesignal_default)
        alertDialog.setButton(-1, "OK") { dialog, which ->
            refreshNotiList()
        }
        alertDialog.show()
    }

    fun showDialog(title: String?, message: String?, launchURL: String?) {
        val alertDialog = AlertDialog.Builder(this@Notification).create()
        alertDialog.setTitle(title)
        if(!message.isNullOrBlank() || !message.isNullOrEmpty()) {
            alertDialog.setMessage(message)
        }
        alertDialog.setIcon(R.drawable.ic_stat_onesignal_default)
        alertDialog.setButton(-1, "OK") { dialog, which ->
            refreshNotiList()
        }

        var btnText = "Open Webkiosk"
        if (!launchURL!!.contains("webkiosk.juit.ac.in")) {
            btnText = "Open Link"
        }
        if (launchURL.endsWith(".pdf")) {
            btnText = "Download"
        }
        val finalBtnText = btnText
        alertDialog.setButton(-3, btnText) { dialog, which ->
            Log.e("handleIntent", "" + finalBtnText)
            if (finalBtnText == "Download") {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(launchURL))
                startActivity(browserIntent)
            } else {
                val finestWebView = FinestWebView.Builder(this@Notification)
                finestWebView.show(launchURL)
            }
        }

        alertDialog.show()
    }

    class NotiAdapter(activity : Activity,notifcationList: List<Notification>) : BaseAdapter(){

        var activity:Activity = activity
        var notiList: List<Notification> = notifcationList
        var inflater:LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;

        override fun getCount(): Int {
            return notiList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): juit.webkiosk.model.Notification {
            return notiList.get(position)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.layout_notification_row, null)
            }

            val txtMessage = view!!.findViewById<TextView>(R.id.title)
            val txtTimestamp = view.findViewById<TextView>(R.id.timestamp)

            var noti = notiList.get(position)
            if(noti.isNew.equals("Y")){
                txtMessage.setTextColor(Color.parseColor("#039BE5"));
            }
            txtMessage.setText(noti.title)

            val RTime: Long = noti.RTime!!.toLong();
            val ago = DateUtils.getRelativeTimeSpanString(RTime, System.currentTimeMillis(),0L, DateUtils.FORMAT_ABBREV_ALL)
            txtTimestamp.text = ago.toString()

            return view
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}