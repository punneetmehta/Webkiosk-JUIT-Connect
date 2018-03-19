package juit.webkiosk.model

/**
 * Created by puneet on 15/11/17.
 */

class CG_SG_Response {
    var error = ""
        internal set
    var total: Int = 0
        internal set
    var cgsg: List<CG_SG_ResponseSingle>? = null
        internal set
}