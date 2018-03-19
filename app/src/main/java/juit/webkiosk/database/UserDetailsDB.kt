package juit.webkiosk.database

import java.util.ArrayList
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import juit.webkiosk.model.UserDetailsResponse

/**
 * Created by puneet on 05/10/17.
 */

class UserDetailsDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_USER_DETAILS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_STUDENT_NAME + " TEXT,"
                + KEY_STUDENT_FATHER_NAME + " TEXT,"
                + KEY_STUDENT_MOBILE + " TEXT,"
                + KEY_STUDENT_EMAIL + " TEXT,"
                + KEY_PARENT_MOBILE + " TEXT,"
                + KEY_PARENT_EMAIL + " TEXT,"
                + KEY_STUDENT_COURSE + " TEXT,"
                + KEY_STUDENT_ENRNO + " TEXT" + ")")
        db.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAILS)
        onCreate(db)
    }

    fun clearDatabase() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DETAILS)
        onCreate(db)
    }

    fun addDetails(userDetails: UserDetailsResponse) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_STUDENT_NAME, userDetails.name)
        values.put(KEY_STUDENT_FATHER_NAME, userDetails.FatherName)
        values.put(KEY_STUDENT_MOBILE, userDetails.StudentMobile)
        values.put(KEY_STUDENT_EMAIL, userDetails.StudentEmail)
        values.put(KEY_PARENT_MOBILE, userDetails.ParentMobile)
        values.put(KEY_PARENT_EMAIL, userDetails.ParentEmail)
        values.put(KEY_STUDENT_COURSE, userDetails.Course)
        values.put(KEY_STUDENT_ENRNO, userDetails.EnrNo)
        db.insert(TABLE_USER_DETAILS, null, values)
        db.close()
    }

    fun getDetails(enrNo: String): UserDetailsResponse {
        val db = this.readableDatabase

        val cursor = db.query(TABLE_USER_DETAILS, arrayOf(KEY_ID, KEY_STUDENT_NAME, KEY_STUDENT_FATHER_NAME, KEY_STUDENT_MOBILE, KEY_STUDENT_EMAIL, KEY_PARENT_MOBILE, KEY_PARENT_EMAIL, KEY_STUDENT_COURSE, KEY_STUDENT_ENRNO),
                KEY_STUDENT_ENRNO + "=?", arrayOf(enrNo), null, null, null, null)
        cursor?.moveToFirst()

        val userDetails = UserDetailsResponse()
        userDetails.name = cursor!!.getString(1)
        userDetails.FatherName = cursor.getString(2)
        userDetails.StudentMobile = cursor.getString(3)
        userDetails.StudentEmail = cursor.getString(4)
        userDetails.ParentMobile = cursor.getString(5)
        userDetails.ParentEmail = cursor.getString(6)
        userDetails.Course = cursor.getString(7)
        userDetails.EnrNo = cursor.getString(8)
        return userDetails
    }

    fun updateDetails(userDetails: UserDetailsResponse): Int {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_STUDENT_NAME, userDetails.name)
        values.put(KEY_STUDENT_FATHER_NAME, userDetails.FatherName)
        values.put(KEY_STUDENT_MOBILE, userDetails.StudentMobile)
        values.put(KEY_STUDENT_EMAIL, userDetails.StudentEmail)
        values.put(KEY_PARENT_MOBILE, userDetails.ParentMobile)
        values.put(KEY_PARENT_EMAIL, userDetails.ParentEmail)
        values.put(KEY_STUDENT_COURSE, userDetails.Course)
        return db.update(TABLE_USER_DETAILS, values, KEY_STUDENT_ENRNO + " = ?",
                arrayOf(userDetails.EnrNo.toString()))
    }

    fun deleteDetails(enrNo: String) {
        val db = this.writableDatabase
        db.delete(TABLE_USER_DETAILS, KEY_STUDENT_ENRNO + " = ?",
                arrayOf(enrNo))
        db.close()
    }

    fun checkIfExist(enrNo: String): Int {
        val countQuery = "SELECT  * FROM $TABLE_USER_DETAILS WHERE $KEY_STUDENT_ENRNO='$enrNo'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    companion object {

        private val DATABASE_VERSION = 1

        private val DATABASE_NAME = "userDetails"
        private val TABLE_USER_DETAILS = "details"

        private val KEY_ID = "id"
        private val KEY_STUDENT_NAME = "name"
        private val KEY_STUDENT_FATHER_NAME = "father_name"
        private val KEY_STUDENT_MOBILE = "student_mobile"
        private val KEY_STUDENT_EMAIL = "student_email"
        private val KEY_PARENT_MOBILE = "parent_mobile"
        private val KEY_PARENT_EMAIL = "parent_email"
        private val KEY_STUDENT_COURSE = "student_course"
        private val KEY_STUDENT_ENRNO = "student_enrno"
    }

}
