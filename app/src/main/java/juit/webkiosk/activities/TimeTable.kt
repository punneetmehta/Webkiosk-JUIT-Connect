package juit.webkiosk.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.design.widget.TabLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.util.ArrayList
import juit.webkiosk.R
import juit.webkiosk.adapter.DividerItemDecoration
import juit.webkiosk.adapter.TimetableAdapter
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.model.AttendanceResponseSingle
import juit.webkiosk.preference.LoginSessionPref
import juit.webkiosk.preference.TimeTablePref
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*

class TimeTable : AppCompatActivity() {

    private var batchName: String? = null

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    internal var loginSessionPref: LoginSessionPref? = null
    private val refreshIcon: MenuItem? = null

    private var timeTablePref:TimeTablePref? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_table)
        attendanceDB = AttendanceDB(this)
        detailAttendanceDB = DetailAttendanceDB(this)
        loginSessionPref = LoginSessionPref(this)
        timeTablePref = TimeTablePref(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        try {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: Exception) {
            try {
                actionBar!!.setDisplayHomeAsUpEnabled(true)
            } catch (e1: Exception) {

            }

        }

        try {
            supportActionBar!!.subtitle = "BETA v2"
        } catch (e: Exception) {
            try {
                actionBar!!.subtitle = "BETA v2"
            } catch (e1: Exception) {

            }

        }

        setTitle("TimeTable - "+timeTablePref!!.getBatch())

        mViewPager = findViewById<ViewPager>(R.id.container)
        tabLayout = findViewById<TabLayout>(R.id.tabs)
        setUpView();
    }

    private fun setUpView(){
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        tabLayout!!.setupWithViewPager(mViewPager)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = inflater!!.inflate(R.layout.fragment_time_table, container, false)

            Log.e("onCreateView", "day : " + arguments!!.getString(ARG_DAY)!!)
            recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view)
            recyclerView!!.addItemDecoration(DividerItemDecoration(context as Context, LinearLayoutManager.VERTICAL))
            recyclerView!!.setHasFixedSize(false)

            val file = TimeTablePref(context as Context).getTimeTableFile();

            val attendanceList = computeTimeTable(context as Context,file,arguments!!.getString(ARG_DAY))

            attendanceAdapter = TimetableAdapter(context as Context, attendanceList, object : TimetableAdapter.OnItemClickListener {
                override fun onItemClick(item: AttendanceResponseSingle) {

                }

                override fun onInfoButtonClick(item: AttendanceResponseSingle) {
                    Toast.makeText(context, "remove", Toast.LENGTH_SHORT).show()
                    Log.e("onRemoveButtonClick", "subjectCode : " + item.code!!)
                    Log.e("onRemoveButtonClick", "subjectName : " + item.name!!)
                    Log.e("onRemoveButtonClick", "day : " + arguments!!.getString(ARG_DAY)!!)
                    Log.e("onRemoveButtonClick", "-=-=-=-=-==-=-=-=-=-=-=-=-=-")
                }
            })

            val mLayoutManager = LinearLayoutManager(context)
            recyclerView!!.layoutManager = mLayoutManager
            recyclerView!!.itemAnimator = DefaultItemAnimator()
            recyclerView!!.adapter = attendanceAdapter

            return rootView
        }

        companion object {
            private val ARG_SECTION_NUMBER = "section_number"
            private val ARG_DAY = "section_day"

            fun newInstance(sectionNumber: Int, day: String): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                args.putString(ARG_DAY, day)
                fragment.arguments = args
                return fragment
            }
        }
    }


    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        internal var days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        override fun getItem(position: Int): Fragment {
            return PlaceholderFragment.newInstance(position, days[position])
        }

        override fun getCount(): Int {
            return 6
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val day = days[position].split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val dy = day[0] + day[1] + day[2] + day[3]
            when (position) {
                0 -> return dy
                1 -> return dy
                2 -> return dy
                3 -> return dy
                4 -> return dy
                5 -> return dy
            }
            return null
        }
    }

    companion object {
        internal var attendanceDB: AttendanceDB? = null
        internal var detailAttendanceDB: DetailAttendanceDB? = null
        internal var attendanceAdapter: TimetableAdapter? = null
        internal var recyclerView: RecyclerView? = null


        fun computeTimeTable(context: Context,filePath:String,day: String): List<AttendanceResponseSingle> {
            val attendanceList = ArrayList<AttendanceResponseSingle>()

            Log.e("processTimeTableFile: ", "filePath : "+filePath)
            val file = File(filePath)
            val stream = FileInputStream(file)
            var myStr:StringBuilder = StringBuilder();
            try
            {
                Log.e("processTimeTableFile: ", "file.length() : "+file.length())
                var lines = file.readLines()
                Log.e("processTimeTableFile: ", "#lines: "+lines.size)
                for(line:String in lines) {
                    myStr.append(line);
                }
            }
            catch (e:Exception) {
                e.printStackTrace()
            }
            finally
            {
                stream.close()
            }

            attendanceDB = AttendanceDB(context)
            try {
                var tokener = JSONTokener(myStr.toString());
                var jsonObject = JSONObject(tokener);

                try {
                    var AttendanceObj = jsonObject.getJSONObject(day)
                    var initTime: Int = 900;
                    while (initTime <= 1700) {

                        var classType = "";
                        try {
                            var temp = AttendanceObj.getJSONObject(initTime.toString());
                            var att = AttendanceResponseSingle()

                            classType = temp.getString("classType")
                            if (classType.equals("L")) classType = "Lecture"
                            if (classType.equals("T")) classType = "Tutorial"
                            if (classType.equals("P")) classType = "Lab"

                            val subjectName = temp.getString("subjectName")
                            val subjectCode = temp.getString("subjectCode")
                            val facultyCode = temp.getString("facultyCode")
                            val classLocation = temp.getString("classLocation")

                            var time = "";
                            if (initTime < 1200) {
                                time = initTime.toString().replace("00", "") + " AM ";
                            } else if(initTime == 1200){
                                time = initTime.toString().replace("00", "") + " PM ";
                            }
                            else {
                                time = (initTime - 1200).toString().replace("00", "") + " PM ";
                            }
                            att.code = subjectCode + " - " + classType + " - " + classLocation + " @ " + time;
                            att.name = subjectName

                            if (attendanceDB!!.checkIfExist(subjectCode) == 1) {
                                var tempObj = attendanceDB!!.getAttendance(subjectCode);
                                att.name = tempObj.name
                                att.Lecture = tempObj.Lecture;
                                att.Tutorial = tempObj.Tutorial;
                                att.Total = tempObj.Total;
                            }

                            attendanceList.add(att)

                        } catch (exc: Exception) {
                            exc.printStackTrace();
                        }

                        if (classType.equals("Lab")) {
                            initTime = initTime + 200;
                        } else {
                            initTime = initTime + 100;
                        }
                    }
                }catch (ex:Exception){

                }
            } catch (exx:Exception) {

            }


            // }
            return attendanceList
        }
    }

}
