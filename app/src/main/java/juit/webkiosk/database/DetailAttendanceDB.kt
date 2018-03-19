package juit.webkiosk.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import java.util.ArrayList

import juit.webkiosk.model.AttendanceResponseSingle
import juit.webkiosk.model.DetailAttendanceResponseSingle

/**
 * Created by puneet on 05/10/17.
 */

class DetailAttendanceDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val allSubjectCode: List<String>
        get() {
            val subjectCodeList = ArrayList<String>()
            val selectQuery = "SELECT $KEY_SUBJECT_CODE FROM $TABLE_USER_DETAIL_ATTENDANCE"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    subjectCodeList.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
            return subjectCodeList
        }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_USER_DETAIL_ATTENDANCE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SUBJECT_CODE + " TEXT,"
                + KEY_FACULTY + " TEXT,"
                + KEY_DAY + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_STATUS + " TEXT" + ")")
        db.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAIL_ATTENDANCE)
        onCreate(db)
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAIL_ATTENDANCE)
        onCreate(db)
    }

    fun getAllAttendance(subjectCode: String): List<DetailAttendanceResponseSingle> {
        val attendanceList = ArrayList<DetailAttendanceResponseSingle>()
        val selectQuery = "SELECT * FROM $TABLE_USER_DETAIL_ATTENDANCE WHERE $KEY_SUBJECT_CODE = '$subjectCode'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val attDetails = DetailAttendanceResponseSingle()
                attDetails.faculty = cursor.getString(2)
                attDetails.day = cursor.getString(3)
                attDetails.date = cursor.getString(4)
                attDetails.time = cursor.getString(5)
                attDetails.status = cursor.getString(6)
                attendanceList.add(attDetails)
            } while (cursor.moveToNext())
        }

        // return contact list
        return attendanceList
    }

    fun addAttendance(subjectCode: String, attDetails: DetailAttendanceResponseSingle) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SUBJECT_CODE, subjectCode)
        values.put(KEY_FACULTY, attDetails.faculty)
        values.put(KEY_DAY, attDetails.day)
        values.put(KEY_DATE, attDetails.date)
        values.put(KEY_TIME, attDetails.time)
        values.put(KEY_STATUS, attDetails.sr)
        db.insert(TABLE_USER_DETAIL_ATTENDANCE, null, values)
        db.close()
    }

    fun getAllTimeByDay(Day: String): List<String> {
        val subjectCodeList = ArrayList<String>()
        val selectQuery = "SELECT DISTINCT($KEY_TIME),$KEY_SUBJECT_CODE FROM $TABLE_USER_DETAIL_ATTENDANCE WHERE $KEY_DAY='$Day'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                subjectCodeList.add(cursor.getString(0) + "_" + cursor.getString(1))
            } while (cursor.moveToNext())
        }
        return subjectCodeList
    }

    fun countFrequency(subjectCode: String, Day: String, time: String): Int {
        val countQuery = "SELECT * FROM " + TABLE_USER_DETAIL_ATTENDANCE + " WHERE " + KEY_SUBJECT_CODE + "='" + subjectCode + "' " +
                "AND " + KEY_DAY + " = '" + Day + "' AND " + KEY_TIME + " = '" + time + "'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun deleteDetails(subjectCode: String) {
        val db = this.writableDatabase
        db.delete(TABLE_USER_DETAIL_ATTENDANCE, KEY_SUBJECT_CODE + " = ?",
                arrayOf(subjectCode))
        db.close()
    }

    fun checkIfExist(subjectCode: String, date: String, time: String): Int {
        val countQuery = "SELECT  * FROM " + TABLE_USER_DETAIL_ATTENDANCE + " WHERE " + KEY_SUBJECT_CODE + "='" + subjectCode + "' " +
                "AND " + KEY_DATE + " = '" + date + "' AND " + KEY_TIME + " = '" + time + "'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    companion object {
        private val DATABASE_VERSION = 1

        private val DATABASE_NAME = "userDetailAttendance"
        private val TABLE_USER_DETAIL_ATTENDANCE = "detailAttendance"

        private val KEY_ID = "id"
        private val KEY_SUBJECT_CODE = "subjectCode"
        private val KEY_FACULTY = "faculty"
        private val KEY_DAY = "day"
        private val KEY_DATE = "date"
        private val KEY_TIME = "time"
        private val KEY_STATUS = "status"
    }
}
