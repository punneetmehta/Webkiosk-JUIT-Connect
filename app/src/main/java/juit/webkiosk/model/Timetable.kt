package juit.webkiosk.model

import android.util.Log

/**
 * Created by puneet on 06/10/17.
 */

class Timetable(var day: String, var subjectCode: String, var time: String) {

    override fun toString(): String {
        return "Day : ${day} | SubjectCode : ${subjectCode} | Time : ${time}"
    }
}
