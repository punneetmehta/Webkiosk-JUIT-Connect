package juit.webkiosk.activities.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import droidmentor.helper.Retrofit.APIListener
import juit.webkiosk.R
import juit.webkiosk.activities.DetailAttendance
import juit.webkiosk.activities.Login
import juit.webkiosk.activities.TimeTable
import juit.webkiosk.adapter.AttendanceAdapter
import juit.webkiosk.adapter.DividerItemDecoration
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.database.NotificationsDB
import juit.webkiosk.database.UserDetailsDB
import juit.webkiosk.helper.ConnectivityReceiver
import juit.webkiosk.helper.JUITKioskAPI
import juit.webkiosk.model.*
import juit.webkiosk.preference.LoginSessionPref
import juit.webkiosk.preference.firstTimeLaunch
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by puneet on 03/11/17.
 */

class AttendanceFragment : Fragment() {

    internal var attendanceList: List<AttendanceResponseSingle> = ArrayList()
    internal var attendanceAdapter: AttendanceAdapter? = null
    internal var recyclerView: RecyclerView? = null
    internal var userDetailsDB: UserDetailsDB? = null
    internal var attendanceDB: AttendanceDB? = null
    internal var detailAttendanceDB: DetailAttendanceDB? = null
    internal var loginSessionPref: LoginSessionPref? = null
    private var isInternetAvailable: Boolean? = false
    internal lateinit var swipeContainer: SwipeRefreshLayout
    internal lateinit var subjectNameToLaunch: String
    internal lateinit var subjectCodeToLaunch: String
    internal var toLaunchDetailActivity = 0
    internal var getAttendance = 0
    internal var toLaunchTimeTable = 0
    internal var fTL: firstTimeLaunch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSubTitle("")
        userDetailsDB = UserDetailsDB(context as Context)
        attendanceDB = AttendanceDB(context as Context)
        detailAttendanceDB = DetailAttendanceDB(context as Context)
        loginSessionPref = LoginSessionPref(context as Context)
        isInternetAvailable = ConnectivityReceiver.isConnected
        fTL = firstTimeLaunch(context as Context)
        attendanceList = attendanceDB!!.allAttendance

        Collections.sort(attendanceList, Comparator { o1, o2 ->
            try {
                val att1 = Integer.parseInt(o1.Total)
                val att2 = Integer.parseInt(o2.Total)
                return@Comparator att1.compareTo(att2)
            } catch (e: Exception) {

            }

            1
        })

        attendanceAdapter = getAttendanceAdapter(attendanceList)

        if ((!fTL!!.getAttendanceActivity()!!)!!) {
            if ((!checkIfDetailAttendanceSynced()!!)!!) {
                getAllDetailAttendance()
            }
        }
    }

    fun getAttendanceAdapter(attList : List<AttendanceResponseSingle>): AttendanceAdapter? {
        attendanceAdapter = AttendanceAdapter((activity as Activity).applicationContext, activity as Activity, attList, object : AttendanceAdapter.OnItemClickListener {
            override fun onItemClick(item: AttendanceResponseSingle) {
                val subjectCode = item.code
                val detailAttData = item.detailAttendanceDATA
                val detailAttendanceList = detailAttendanceDB!!.getAllAttendance(subjectCode!!)
                if (detailAttendanceList.size == 0) {
                    // Fetch Detail Attendance
                    toLaunchDetailActivity = 1
                    subjectCodeToLaunch = subjectCode
                    subjectNameToLaunch = item.name!!
                    try {
                        val kAPI = JUITKioskAPI(activity as Activity, DetailAttendanceResponse::class.java, detailAttendanceListener)
                        kAPI.getDetailAttendance(loginSessionPref!!.sessionKey, subjectCode, detailAttData!!, "")
                        Toast.makeText(activity, "Getting Detailed Attendance..", Toast.LENGTH_SHORT).show()
                    } catch(e : Exception){
                        Toast.makeText(activity, "Attendance Not Updated Yet...", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    var total:Int = 0;
                    try {
                        total = Integer.parseInt(item.Total);
                    }
                    catch (e:Exception){
                    }
                    if(total == 0){
                        Toast.makeText(activity, "Attendance Not Updated Yet...", Toast.LENGTH_SHORT).show()
                    } else {
                        val i = Intent(activity, DetailAttendance::class.java)
                        i.putExtra("subjectName", item.name)
                        i.putExtra("subjectCode", subjectCode)
                        startActivity(i)
                    }
                }
            }

            override fun onInfoButtonClick(item: AttendanceResponseSingle) {
                var totalClasses = 0
                var totalPresent = 0
                val subjectCode = item.code
                val detailAttData = item.detailAttendanceDATA
                val detailAttendanceList = detailAttendanceDB!!.getAllAttendance(subjectCode!!)
                totalClasses = detailAttendanceList.size
                if (totalClasses != 0) {
                    for (attDet in detailAttendanceList) {
                        if (attDet.status == "Present") {
                            totalPresent++
                        }
                    }
                    val totalAbsent = totalClasses - totalPresent
                    val attendanceNow = Math.floor(java.lang.Double.parseDouble(totalPresent.toString()) / java.lang.Double.parseDouble(totalClasses.toString()) * 100).toInt()
                    // After 1 More Class
                    val ifTotalClasses = totalClasses + 1
                    val ifPresent = totalPresent + 1
                    val ifAbsent = totalPresent

                    val attendanceIfPresent = Math.floor(java.lang.Double.parseDouble(ifPresent.toString()) / java.lang.Double.parseDouble(ifTotalClasses.toString()) * 100).toInt()
                    val attendanceIfAbsent = Math.floor(java.lang.Double.parseDouble(ifAbsent.toString()) / java.lang.Double.parseDouble(ifTotalClasses.toString()) * 100).toInt()


                    // After 3 More Class
                    val ifTotalClasses_3 = totalClasses + 3
                    val ifPresent_3 = totalPresent + 3
                    val ifAbsent_3 = totalPresent

                    val attendanceIfPresent_3 = Math.floor(java.lang.Double.parseDouble(ifPresent_3.toString()) / java.lang.Double.parseDouble(ifTotalClasses_3.toString()) * 100).toInt()
                    val attendanceIfAbsent_3 = Math.floor(java.lang.Double.parseDouble(ifAbsent_3.toString()) / java.lang.Double.parseDouble(ifTotalClasses_3.toString()) * 100).toInt()

                    // After 5 More Class
                    val ifTotalClasses_5 = totalClasses + 5
                    val ifPresent_5 = totalPresent + 5
                    val ifAbsent_5 = totalPresent

                    val attendanceIfPresent_5 = Math.floor(java.lang.Double.parseDouble(ifPresent_5.toString()) / java.lang.Double.parseDouble(ifTotalClasses_5.toString()) * 100).toInt()
                    val attendanceIfAbsent_5 = Math.floor(java.lang.Double.parseDouble(ifAbsent_5.toString()) / java.lang.Double.parseDouble(ifTotalClasses_5.toString()) * 100).toInt()

                    val title = item.name
                    val message = "Total Classes : " + totalClasses + "\n" +
                            "Classes Attended : " + totalPresent + "\n" +
                            "Current Attendance : " + attendanceNow + "%\n\n" +
                            "Attendance If you\n" +
                            "   Attend next Class : " + attendanceIfPresent + "%\n" +
                            "   Miss next Class : " + attendanceIfAbsent + "%\n\n" +
                            "   Attend next 3 Class : " + attendanceIfPresent_3 + "%\n" +
                            "   Miss next 3 Class : " + attendanceIfAbsent_3 + "%\n\n" +
                            "   Attend next 5 Class : " + attendanceIfPresent_5 + "%\n" +
                            "   Miss next 5 Class : " + attendanceIfAbsent_5 + "%"

                    val alertDialog = AlertDialog.Builder(activity).create()
                    alertDialog.setTitle(title)
                    alertDialog.setMessage(message)
                    alertDialog.setButton(-1, "OK") { dialog, which -> }
                    alertDialog.show()
                } else {
                    Toast.makeText(activity, "Getting Detailed Attendance...", Toast.LENGTH_SHORT).show()
                    val kAPI = JUITKioskAPI(activity as Activity, DetailAttendanceResponse::class.java, detailAttendanceListener)
                    kAPI.getDetailAttendance(loginSessionPref!!.sessionKey, subjectCode, detailAttData!!, "")
                }
            }

        })
        return attendanceAdapter
    }

    override fun onResume() {
        super.onResume()
        stopRefresh()
        val mLayoutManager = LinearLayoutManager((activity as Activity).applicationContext)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = attendanceAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater!!.inflate(R.layout.fragment_attendance, container, false) as View
        recyclerView = view!!.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView!!.addItemDecoration(DividerItemDecoration(activity as Activity, LinearLayoutManager.VERTICAL))

        swipeContainer = view!!.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        swipeContainer!!.setOnRefreshListener {
            val kAPI = JUITKioskAPI(activity as Activity, AttendanceResponse::class.java, attendanceListener)
            if(kAPI.isNetworkAvailable((activity as Activity).applicationContext) && kAPI.isInternetAvailable()){
                setSubTitle("Syncing...")
                startRefersh()
            } else {
                stopRefresh();
            }
            kAPI.getAttendance(loginSessionPref!!.sessionKey, "")
        }
        return view
    }

    internal var loginListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val loginResult = (res as LoginResponse).loginResult
            val loginResponse = res.response
            val loginCookies = res.loginCookies

            if (loginResult == 1) {
                loginSessionPref!!.sessionKey = loginCookies!!
                setSubTitle("")
                stopRefresh()

                if (getAttendance == 1) {
                    setSubTitle("Syncing...")
                    startRefersh()
                    val kAPI = JUITKioskAPI((activity as Activity), AttendanceResponse::class.java, attendanceListener)
                    kAPI.getAttendance(loginSessionPref!!.sessionKey, "")
                }
                return ;
            }
            Toast.makeText(activity, "Invalid Login Details...", Toast.LENGTH_SHORT).show()
            logout()
        }

        override fun onFailure(from: Int, t: Throwable) {
            stopRefresh()
            Toast.makeText(activity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            stopRefresh()
            Toast.makeText(activity, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    internal var detailAttendanceListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as DetailAttendanceResponse).error
            val subjectCode = res.subjectCode
            detailAttendanceDB!!.deleteDetails(subjectCode!!)
            if (error.isEmpty() || error == null) {
                val detailAttendanceResponseSingleList = res.DetailAttendance
                for (DetailAttendanceResponse in detailAttendanceResponseSingleList!!) {
                    val partialDate = DetailAttendanceResponse.date
                    val splitDate = partialDate!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val date = splitDate[0]
                    val _time = splitDate[1]
                    var _ampm = splitDate[2]
                    val _t = Integer.parseInt(_time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
                    if (_t == 12 || _t < 9) {
                        if (_ampm == "am") {
                            _ampm = "pm"
                        }
                    }
                    val time = _time + " " + _ampm
                    val format1 = SimpleDateFormat("dd-MM-yyyy")
                    var dt1: Date? = null
                    try {
                        dt1 = format1.parse(date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    val dayOfTheWeek = DateFormat.format("EEEE", dt1) as String

                    if (detailAttendanceDB!!.checkIfExist(subjectCode, date, time) == 0) {
                        // Insert
                        val detailAttendance = DetailAttendanceResponseSingle()
                        detailAttendance.faculty = DetailAttendanceResponse.faculty
                        detailAttendance.day = dayOfTheWeek
                        detailAttendance.date = date
                        detailAttendance.time = time
                        detailAttendance.sr = DetailAttendanceResponse.sr
                        detailAttendanceDB!!.addAttendance(subjectCode, detailAttendance)
                    } else {
                        // Update
                    }
                }
                //Toast.makeText(Attendance.this,"Detail Attendance Ready",Toast.LENGTH_SHORT).show();
                if (toLaunchTimeTable == 1) {
                    if (checkIfDetailAttendanceSynced()!!) {
                        val i = Intent((activity as Activity), TimeTable::class.java)
                        toLaunchTimeTable = 0
                        (activity as Activity).applicationContext.startActivity(i)
                    }
                }

                if (toLaunchDetailActivity == 1) {

                    val i = Intent(activity, DetailAttendance::class.java)
                    i.putExtra("subjectName", subjectNameToLaunch)
                    i.putExtra("subjectCode", subjectCodeToLaunch)
                    toLaunchDetailActivity = 0
                    subjectNameToLaunch = ""
                    subjectCodeToLaunch = ""
                    (activity as Activity).startActivity(i)
                }

                if (getAttendance == 1) {
                    setSubTitle("Syncing...")
                    getAttendance = 0
                    val kioskAPI = JUITKioskAPI((activity as Activity), AttendanceResponse::class.java, attendanceListener)
                    kioskAPI.getAttendance(loginSessionPref!!.sessionKey, "")
                }

                checkIfDetailAttendanceSynced()
            } else {
                Toast.makeText(activity, "Login Token Expired, Pull down to Refresh.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(from: Int, t: Throwable) {}

        override fun onNetworkFailure(from: Int) {}
    }

    internal var attendanceListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as AttendanceResponse).error
            if (error.isEmpty() || error == null) {
                val subjectCodeList = ArrayList<String>()
                var subjectCodeListInDB: MutableList<String> = ArrayList()
                val attendanceResponseSingleList = res.attendance
                for (attendanceResponse in attendanceResponseSingleList!!) {
                    subjectCodeList.add(attendanceResponse.code!!)
                    if (attendanceDB!!.checkIfExist(attendanceResponse.code!!) > 0) {
                        // Update
                        attendanceDB!!.updateAttendance(attendanceResponse)
                    } else {
                        // Insert
                        attendanceDB!!.addAttendance(attendanceResponse)
                    }

                }
                subjectCodeListInDB = attendanceDB!!.allSubjectCode as MutableList<String>

                subjectCodeListInDB.removeAll(subjectCodeList)
                for (subjectCode in subjectCodeListInDB) {
                    // Delete them { in case of Change of Subjects, OLD Subjects Need to be Deleted }
                    attendanceDB!!.deleteDetails(subjectCode)
                }


                val attList = attendanceDB!!.allAttendance

                Collections.sort(attList, Comparator { o1, o2 ->
                    try {
                        val att1 = Integer.parseInt(o1.Total)
                        val att2 = Integer.parseInt(o2.Total)
                        return@Comparator att1.compareTo(att2)
                    } catch (e: Exception) {

                    }

                    1
                })

                recyclerView!!.adapter = getAttendanceAdapter(attList)
                recyclerView!!.invalidate()
                setSubTitle("");
                stopRefresh();
                getAllDetailAttendance()

            } else {
                setSubTitle("Updating Login Token...")
                getAttendance = 1
                val kAPI = JUITKioskAPI((activity as Activity), LoginResponse::class.java, loginListener)
                kAPI.processLogin(loginSessionPref!!.username, loginSessionPref!!.password, "")
            }
        }

        override fun onFailure(from: Int, t: Throwable) {
            stopRefresh()
            Toast.makeText(activity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            stopRefresh()
        }
    }


    fun setSubTitle(message: String) {
        try {
            val act = activity as AppCompatActivity
            act!!.supportActionBar!!.subtitle = message
        } catch (e: Exception) {
            try {
                (activity as Activity).actionBar!!.subtitle = message
            } catch (e1: Exception) {

            }
        }
    }

    fun startRefersh() {
        swipeContainer.isRefreshing = true
    }

    fun stopRefresh() {
        swipeContainer.isRefreshing = false
        setSubTitle("")
    }

    fun logout() {
        // Logout
        loginSessionPref!!.sessionKey = ""
        loginSessionPref!!.username = ""
        loginSessionPref!!.password = ""
        userDetailsDB!!.clearDatabase()
        attendanceDB!!.clearDatabase()
        detailAttendanceDB!!.clearDatabase()
        fTL!!.detailAttendanceSync = true
        NotificationsDB(activity as Activity)!!.clearDatabase()
        // Empty All DBs
        val i = Intent(activity, Login::class.java)
        (activity as Activity).startActivity(i)
        (activity as Activity).finish()
    }


    fun checkIfDetailAttendanceSynced(): Boolean? {
        var pass = 0
        val attList = attendanceDB!!.allAttendance
        for (att in attList) {
            val subjectCode = att.code
            val detailAttData = att.detailAttendanceDATA
            val detailAttList = detailAttendanceDB!!.getAllAttendance(subjectCode!!)
            if (detailAttList.size == 0) {
                pass++
                if (pass > 1) {
                    return false
                }
            }
        }
        fTL!!.detailAttendanceSync = true
        return true
    }

    fun getAllDetailAttendance() {
        val attList = attendanceDB!!.allAttendance
        for (att in attList) {
            try {
                val subjectCode = att.code
                val detailAttData = att.detailAttendanceDATA
                val kAPI = JUITKioskAPI(activity as Activity, DetailAttendanceResponse::class.java, detailAttendanceListener)
                kAPI.getDetailAttendance(loginSessionPref!!.sessionKey, subjectCode!!, detailAttData!!, "")
            } catch (e:Exception){

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {

        fun newInstance(title: String): AttendanceFragment {
            val fragment = AttendanceFragment()
            val args = Bundle()
            //args.putString(KEY_MOVIE_TITLE, movieTitle);
            fragment.arguments = args
            return fragment
        }
    }
}