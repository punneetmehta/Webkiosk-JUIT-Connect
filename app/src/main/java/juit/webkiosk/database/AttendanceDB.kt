package juit.webkiosk.database

import java.util.ArrayList
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import juit.webkiosk.model.AttendanceResponseSingle
import juit.webkiosk.model.UserDetailsResponse

/**
 * Created by puneet on 05/10/17.
 */

class AttendanceDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val allSubjectCode: List<String>
        get() {
            val subjectCodeList = ArrayList<String>()
            val selectQuery = "SELECT $KEY_SUBJECT_CODE FROM $TABLE_USER_ATTENDANCE"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    subjectCodeList.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
            return subjectCodeList
        }

    // return contact list
    val allAttendance: List<AttendanceResponseSingle>
        get() {
            val attendanceList = ArrayList<AttendanceResponseSingle>()
            val selectQuery = "SELECT  * FROM " + TABLE_USER_ATTENDANCE
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val attDetails = AttendanceResponseSingle()
                    attDetails.name = cursor.getString(1)
                    attDetails.code = cursor.getString(2)
                    attDetails.detailAttendanceDATA = cursor.getString(3)
                    attDetails.Lecture = cursor.getString(4)
                    attDetails.Tutorial = cursor.getString(5)
                    attDetails.Total = cursor.getString(6)
                    attendanceList.add(attDetails)
                } while (cursor.moveToNext())
            }
            return attendanceList
        }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE " + TABLE_USER_ATTENDANCE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SUBJECT_NAME + " TEXT,"
                + KEY_SUBJECT_CODE + " TEXT,"
                + KEY_DETAIL_DATA + " TEXT,"
                + KEY_LECTURE_ATT + " TEXT,"
                + KEY_TUTORIAL_ATT + " TEXT,"
                + KEY_TOTAL_ATT + " TEXT" + ")")
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ATTENDANCE)
        onCreate(db)
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ATTENDANCE)
        onCreate(db)
    }

    fun addAttendance(attDetails: AttendanceResponseSingle) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SUBJECT_NAME, attDetails.name)
        values.put(KEY_DETAIL_DATA, attDetails.detailAttendanceDATA)
        values.put(KEY_LECTURE_ATT, attDetails.Lecture)
        values.put(KEY_TUTORIAL_ATT, attDetails.Tutorial)
        values.put(KEY_TOTAL_ATT, attDetails.Total)
        values.put(KEY_SUBJECT_CODE, attDetails.code)
        db.insert(TABLE_USER_ATTENDANCE, null, values)
        db.close()
    }

    fun getAttendance(subjectCode: String): AttendanceResponseSingle {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_USER_ATTENDANCE, arrayOf(KEY_ID, KEY_SUBJECT_NAME, KEY_SUBJECT_CODE, KEY_DETAIL_DATA, KEY_LECTURE_ATT, KEY_TUTORIAL_ATT, KEY_TOTAL_ATT),
                KEY_SUBJECT_CODE + "=?", arrayOf(subjectCode), null, null, null, null)
        cursor?.moveToFirst()

        val attDetails = AttendanceResponseSingle()
        attDetails.name = cursor!!.getString(1)
        attDetails.code = cursor.getString(2)
        attDetails.detailAttendanceDATA = cursor.getString(3)
        attDetails.Lecture = cursor.getString(4)
        attDetails.Tutorial = cursor.getString(5)
        attDetails.Total = cursor.getString(6)
        return attDetails
    }

    fun updateAttendance(attDetails: AttendanceResponseSingle): Int {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_SUBJECT_NAME, attDetails.name)
        values.put(KEY_DETAIL_DATA, attDetails.detailAttendanceDATA)
        values.put(KEY_LECTURE_ATT, attDetails.Lecture)
        values.put(KEY_TUTORIAL_ATT, attDetails.Tutorial)
        values.put(KEY_TOTAL_ATT, attDetails.Total)

        return db.update(TABLE_USER_ATTENDANCE, values, KEY_SUBJECT_CODE + " = ?",
                arrayOf(attDetails.code.toString()))
    }

    fun deleteDetails(subjectCode: String) {
        val db = this.writableDatabase
        db.delete(TABLE_USER_ATTENDANCE, KEY_SUBJECT_CODE + " = ?",
                arrayOf(subjectCode))
        db.close()
    }

    fun checkIfExist(subjectCode: String): Int {
        val countQuery = "SELECT  * FROM $TABLE_USER_ATTENDANCE WHERE $KEY_SUBJECT_CODE='$subjectCode'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    companion object {
        private val DATABASE_VERSION = 1

        private val DATABASE_NAME = "userAttendance"
        private val TABLE_USER_ATTENDANCE = "attendance"

        private val KEY_ID = "id"
        private val KEY_SUBJECT_NAME = "subjectName"
        private val KEY_SUBJECT_CODE = "subjectCode"
        private val KEY_DETAIL_DATA = "detailData"
        private val KEY_LECTURE_ATT = "lecture"
        private val KEY_TUTORIAL_ATT = "tutorial"
        private val KEY_TOTAL_ATT = "total"
    }

}
