package juit.webkiosk.database

import java.util.ArrayList
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import juit.webkiosk.model.AttendanceResponseSingle
import juit.webkiosk.model.Notification

/**
 * Created by puneet on 18/10/17.
 */

class NotificationsDB(context : Context) : SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION){

    fun addNotification(noti: Notification) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NotificationsDB.KEY_ID, noti.id)
        values.put(NotificationsDB.KEY_TITLE, noti.title)
        values.put(NotificationsDB.KEY_MESSAGE, noti.message)
        values.put(NotificationsDB.KEY_URL, noti.link)
        values.put(NotificationsDB.KEY_R,System.currentTimeMillis().toString());
        values.put(NotificationsDB.KEY_IS_NEW,"Y");
        db.insert(NotificationsDB.TABLE_NOTIFICATIONS, null, values)
        db.close()
    }

    val allNotifications: List<Notification>
        get() {
            val notificationList = ArrayList<Notification>()
            val selectQuery = "SELECT  * FROM " + NotificationsDB.TABLE_NOTIFICATIONS
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val noti = Notification()
                    noti.id = cursor.getString(0);
                    noti.title = cursor.getString(1);
                    noti.message = cursor.getString(2);
                    noti.link = cursor.getString(3);
                    noti.RTime = cursor.getString(4);
                    noti.isNew = cursor.getString(5);
                    notificationList.add(noti)
                } while (cursor.moveToNext())
            }
            return notificationList
        }

    fun notificationsCount() : Int{
        return allNotifications.size
    }

    fun unreadNotificationCount(): Int {
        val countQuery = "SELECT  * FROM ${NotificationsDB.TABLE_NOTIFICATIONS} WHERE ${NotificationsDB.KEY_IS_NEW}='Y'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun getNotification(notiID: String): Notification {
        val db = this.readableDatabase
        val cursor = db.query(NotificationsDB.TABLE_NOTIFICATIONS, arrayOf(NotificationsDB.KEY_ID, NotificationsDB.KEY_TITLE, NotificationsDB.KEY_MESSAGE, NotificationsDB.KEY_URL, NotificationsDB.KEY_R, NotificationsDB.KEY_IS_NEW),
                NotificationsDB.KEY_ID + "=?", arrayOf(notiID), null, null, null, null)
        cursor?.moveToFirst()

        val noti = Notification()
        noti.id = cursor.getString(0);
        noti.title = cursor.getString(1);
        noti.message = cursor.getString(2);
        noti.link = cursor.getString(3);
        noti.RTime = cursor.getString(4);
        return noti
    }

    fun markNotificationAsRead(noti: Notification): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NotificationsDB.KEY_ID, noti.id)
        values.put(NotificationsDB.KEY_TITLE, noti.title)
        values.put(NotificationsDB.KEY_MESSAGE, noti.message)
        values.put(NotificationsDB.KEY_URL, noti.link)
        values.put(NotificationsDB.KEY_R,noti.RTime);
        values.put(NotificationsDB.KEY_IS_NEW,"N");
        return db.update(NotificationsDB.TABLE_NOTIFICATIONS, values, NotificationsDB.KEY_ID + " = ?", arrayOf(noti.id))
    }

    fun deleteNotification(notiID: String) {
        val db = this.writableDatabase
        db.delete(NotificationsDB.TABLE_NOTIFICATIONS, NotificationsDB.KEY_ID + " = ?", arrayOf(notiID.toString()))
        db.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = ("CREATE TABLE " + NotificationsDB.TABLE_NOTIFICATIONS + "("
                + NotificationsDB.KEY_ID + " TEXT,"
                + NotificationsDB.KEY_TITLE + " TEXT,"
                + NotificationsDB.KEY_MESSAGE + " TEXT,"
                + NotificationsDB.KEY_URL + " TEXT, "
                + NotificationsDB.KEY_R + " TEXT, "
                + NotificationsDB.KEY_IS_NEW + " TEXT" + ")")
        db!!.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + NotificationsDB.TABLE_NOTIFICATIONS)
        onCreate(db)
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + NotificationsDB.TABLE_NOTIFICATIONS)
        onCreate(db)
    }

    companion object {
        private val DATABASE_VERSION = 1

        private val DATABASE_NAME = "userNotifications"
        private val TABLE_NOTIFICATIONS = "notifications"

        private val KEY_ID = "id"
        private val KEY_TITLE = "notiTitle"
        private val KEY_MESSAGE = "notiMessage"
        private val KEY_URL = "notiUrl"
        private val KEY_R = "notiTime";
        private val KEY_IS_NEW = "isNew";
    }
}
