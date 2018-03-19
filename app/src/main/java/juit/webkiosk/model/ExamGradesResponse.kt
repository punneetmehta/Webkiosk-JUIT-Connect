package juit.webkiosk.model

/**
 * Created by puneet on 15/11/17.
 */

class ExamGradesResponse {
    var error = ""
        internal set
    var totalGrades: Int = 0
        internal set
    var grades: List<ExamGradesResponseSingle>? = null
        internal set
}