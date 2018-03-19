package juit.webkiosk.model

import android.util.Log

import com.google.gson.Gson

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by puneet on 15/11/17.
 */

class SubjectRegtdResponse{
    var error = ""
        internal set
    var totalSubjects: Int = 0
        internal set
    var subjects: List<SubjectRegtdResponseSingle>? = null
        internal set
}

