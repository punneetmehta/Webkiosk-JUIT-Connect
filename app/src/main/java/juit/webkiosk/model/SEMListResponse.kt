package juit.webkiosk.model

/**
 * Created by puneet on 15/11/17.
 */

class SEMListResponse {
    var error = ""
        internal set
    var total: Int = 0
        internal set
    var semesters: List<SEMListResponseSingle>? = null
        internal set
}