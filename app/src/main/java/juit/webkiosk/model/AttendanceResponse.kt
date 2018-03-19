package juit.webkiosk.model

import android.util.Log

import com.google.gson.Gson

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by puneet on 05/10/17.
 */

class AttendanceResponse {
    var error = ""
        internal set
    var name: String? = null
        internal set
    var EnrNo: String? = null
        internal set
    var courseAndBranch: String? = null
        internal set
    var currentSem: String? = null
        internal set
    var totalSubjects: Int = 0
        internal set
    var attendance: List<AttendanceResponseSingle>? = null
        internal set
}
