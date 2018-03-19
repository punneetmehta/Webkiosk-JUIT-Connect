package juit.webkiosk.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import droidmentor.helper.Retrofit.APIListener

import juit.webkiosk.R
import juit.webkiosk.activities.navigation.CustomExpandableListAdapter
import juit.webkiosk.activities.navigation.DataSource
import juit.webkiosk.activities.navigation.FragmentNavigationManager
import juit.webkiosk.activities.navigation.NavigationManager
import juit.webkiosk.activities.settings.Settings
import juit.webkiosk.adapter.AttendanceAdapter
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.database.NotificationsDB
import juit.webkiosk.database.UserDetailsDB
import juit.webkiosk.helper.ConnectivityReceiver
import juit.webkiosk.helper.JUITKioskAPI
import juit.webkiosk.model.*
import juit.webkiosk.preference.LoginSessionPref
import juit.webkiosk.preference.TimeTablePref
import juit.webkiosk.preference.firstTimeLaunch
import retrofit2.Response
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by puneet on 03/11/17.
 */

class MainActivity : AppCompatActivity() {

    internal var userDetailsDB: UserDetailsDB? = null
    internal var attendanceDB: AttendanceDB? = null
    internal var detailAttendanceDB: DetailAttendanceDB? = null
    internal var loginSessionPref: LoginSessionPref? = null
    private var isInternetAvailable: Boolean? = false
    internal lateinit var swipeContainer: SwipeRefreshLayout
    internal var getAttendance = 0
    internal var toLaunchTimeTable = 0
    internal var fTL: firstTimeLaunch? = null
    internal var SHOWCASE_ID = "mainActivity_showCase"

    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var mActivityTitle: String? = null

    private var mExpandableListView: ExpandableListView? = null
    private var mExpandableListAdapter: ExpandableListAdapter? = null
    private var mExpandableListTitle: MutableList<String>? = null
    private var mNavigationManager: NavigationManager? = null

    private var mExpandableListData: Map<String, List<String>>? = null

    private val TIME_INTERVAL = 2000 // # milliseconds, desired time passed between two back presses.
    private var mBackPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userDetailsDB = UserDetailsDB(this@MainActivity)
        attendanceDB = AttendanceDB(this@MainActivity)
        detailAttendanceDB = DetailAttendanceDB(this@MainActivity)
        loginSessionPref = LoginSessionPref(this@MainActivity)
        isInternetAvailable = ConnectivityReceiver.isConnected
        fTL = firstTimeLaunch(this@MainActivity)

        mDrawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        mActivityTitle = title.toString()

        mExpandableListView = findViewById<ExpandableListView>(R.id.navList)
        mNavigationManager = FragmentNavigationManager.obtain(this)

        mExpandableListData = DataSource.getData(this)
        mExpandableListTitle = ArrayList()
        mExpandableListTitle!!.add(0, "Personal Info")
        mExpandableListTitle!!.add(1, "Academic Info")
        mExpandableListTitle!!.add(2, "Exam Info")

        addDrawerItems()
        setupDrawer()

        if (savedInstanceState == null) {
            selectFirstItemAsDefault()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onResume() {
        super.onResume()

        // Expand List
        mExpandableListView!!.expandGroup(0)
        mExpandableListView!!.expandGroup(1)
        mExpandableListView!!.expandGroup(2)

        if(fTL!!.getMainActivity()!!){
            // First Launch
            // Opening Drawer
            mDrawerLayout!!.openDrawer(GravityCompat.START);
            // Update Menu Title
            supportActionBar!!.title = "Menu"
            fTL!!.setMainActivity(false);
        }

        selectFirstItemAsDefault();
    }

    private fun selectFirstItemAsDefault() {
        if (mNavigationManager != null) {
            mNavigationManager!!.showFragmentAttendance("My Attendance")
            supportActionBar!!.title = "My Attendance"
        }
    }

    private fun addDrawerItems() {
        mExpandableListAdapter = CustomExpandableListAdapter(this, mExpandableListTitle as List<String>, mExpandableListData as Map<String, List<String>>)
        mExpandableListView!!.setAdapter(mExpandableListAdapter)
        mExpandableListView!!.setOnGroupExpandListener { groupPosition ->
            var title = mExpandableListTitle!![groupPosition].toString()
            title = title.replace("1. ", "")
            title = title.replace("2. ", "")
            title = title.replace("3. ", "")
            supportActionBar!!.title = title
        }

        mExpandableListView!!.setOnGroupCollapseListener { }

        mExpandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val selectedItem = (mExpandableListData!![mExpandableListTitle!![groupPosition]] as List<*>)[childPosition].toString()
            supportActionBar!!.title = selectedItem

            if (selectedItem == "Personal Details") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentPersonalDetails(selectedItem)
            } else if (selectedItem == "My Attendance") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentAttendance(selectedItem)
            } else if (selectedItem == "Subject Regtd.") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentSubjectsRegtd(selectedItem)
            } else if (selectedItem == "Subject Faculty") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentSubjectFaculty(selectedItem)
            } else if (selectedItem == "Exam Grades") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentExamGrades(selectedItem)
            } else if (selectedItem == "View SGPA/CGPA") {
                mActivityTitle = selectedItem
                mNavigationManager!!.showFragmentSGPACGPA(selectedItem)
            } else {
                mActivityTitle = "WebKiosk"
            }


            mDrawerLayout!!.closeDrawer(GravityCompat.START)
            false
        }
    }

    private fun setupDrawer() {
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                supportActionBar!!.title = "Menu"
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                supportActionBar!!.title = mActivityTitle
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }

        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        mDrawerLayout!!.setDrawerListener(mDrawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.attendance_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_timetable -> {
                //if ((!checkIfDetailAttendanceSynced()!!)!!) {
                //    toLaunchTimeTable = 1
                //    getAllDetailAttendance()
                //    Toast.makeText(this@MainActivity, "Preparing TimeTable...", Toast.LENGTH_LONG).show()
                //} else {
                if(File(TimeTablePref(this@MainActivity).getTimeTableFile()).exists()) {
                    val i = Intent(this@MainActivity, TimeTable::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this@MainActivity, "No Timetable Available for Your Batch.", Toast.LENGTH_LONG).show()
                }
                //}
                return true
            }
            R.id.action_notifications -> {
                val i = Intent(this@MainActivity, Notification::class.java)
                startActivity(i)
                return true
            }
            R.id.action_settings -> {
                val i = Intent(this@MainActivity, Settings::class.java)
                startActivity(i)
                return true
            }
            R.id.action_logout -> {
                logout()
                return true
            }
        }
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item)

    }

    fun getAllDetailAttendance() {
        val attList = attendanceDB!!.allAttendance
        for (att in attList) {
            val subjectCode = att.code
            val detailAttData = att.detailAttendanceDATA
            val kAPI = JUITKioskAPI(this@MainActivity, DetailAttendanceResponse::class.java, detailAttendanceListener)
            kAPI.getDetailAttendance(loginSessionPref!!.sessionKey, subjectCode!!, detailAttData!!, "")
        }
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

    internal var loginListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val loginResult = (res as LoginResponse).loginResult
            val loginResponse = res.response
            val loginCookies = res.loginCookies

            Log.e("Login", "loginResult : " + loginResult)
            Log.e("Login", "loginResponse : " + loginResponse!!)
            if (loginResult == 1) {
                Log.e("Login", "loginCookies : " + loginCookies!!)
                loginSessionPref!!.sessionKey = loginCookies
                if (getAttendance == 1) {
                    getAllDetailAttendance()
                }
                return ;
            }
            Toast.makeText(this@MainActivity, "Invalid Login Details...", Toast.LENGTH_SHORT).show()
            logout()
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText(this@MainActivity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText(this@MainActivity, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }


    internal var detailAttendanceListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as DetailAttendanceResponse).error
            Log.e("DetailAttendanceResp", "subjectCode : " + res.subjectCode!!)
            val subjectCode = res.subjectCode
            Log.e("DetailAttendanceResp", "DB : Deleting Existing Details for : " + subjectCode!!)
            detailAttendanceDB!!.deleteDetails(subjectCode)
            if (error.isEmpty() || error == null) {
                val detailAttendanceResponseSingleList = res.DetailAttendance
                for (DetailAttendanceResponse in detailAttendanceResponseSingleList!!) {
                    Log.e("DetailAttendanceResp", "Faculty : " + DetailAttendanceResponse.faculty!!)
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
                    Log.e("DetailAttendanceResp", "Day : " + dayOfTheWeek)
                    Log.e("DetailAttendanceResp", "Date : " + date)
                    Log.e("DetailAttendanceResp", "Time : " + time)
                    Log.e("DetailAttendanceResp", "ClassType : " + DetailAttendanceResponse.classType!!)
                    Log.e("DetailAttendanceResp", "Status : " + DetailAttendanceResponse.sr!!)
                    Log.e("DetailAttendanceResp", "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")

                    if (detailAttendanceDB!!.checkIfExist(subjectCode, date, time) == 0) {
                        // Insert
                        Log.e("DetailAttendanceResp", "DB : Inserting Details for : " + subjectCode)
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
                        val i = Intent(this@MainActivity, TimeTable::class.java)
                        toLaunchTimeTable = 0
                        startActivity(i)
                    }
                }

                checkIfDetailAttendanceSynced()
            } else {
                Log.e("Attendance", "ERROR : Login Again")
                Toast.makeText(this@MainActivity, "Login Token Expired, Getting New Token...", Toast.LENGTH_SHORT).show()
                val kAPI = JUITKioskAPI(this@MainActivity, LoginResponse::class.java, loginListener)
                kAPI.processLogin(loginSessionPref!!.username,loginSessionPref!!.password,"")
            }
        }

        override fun onFailure(from: Int, t: Throwable) {}

        override fun onNetworkFailure(from: Int) {}
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
        NotificationsDB(this)!!.clearDatabase()
        // Empty All DBs
        val i = Intent(this@MainActivity, Login::class.java)
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
           // super.onBackPressed()
            finish()
            return
        } else {
            Toast.makeText(baseContext, "Tap back button in order to exit", Toast.LENGTH_SHORT).show()
        }

        mBackPressed = System.currentTimeMillis()
    }
}
