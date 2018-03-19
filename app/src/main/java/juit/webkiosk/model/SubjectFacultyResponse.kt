package juit.webkiosk.model

/**
 * Created by puneet on 15/11/17.
 */

class SubjectFacultyResponse {
    var error = ""
        internal set
    var teachers: List<SubjectFacultyResponseSingle>? = null
        internal set
}