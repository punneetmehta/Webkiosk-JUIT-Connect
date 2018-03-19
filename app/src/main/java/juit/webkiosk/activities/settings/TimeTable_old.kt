package juit.webkiosk.activities.settings

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import juit.webkiosk.R
import juit.webkiosk.adapter.DividerItemDecoration
import juit.webkiosk.adapter.TimetableAdapter
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.model.AttendanceResponseSingle
import juit.webkiosk.model.Timetable
import juit.webkiosk.preference.LoginSessionPref
import java.util.*

class TimeTable_old : AppCompatActivity() {


    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mViewPager: ViewPager? = null
    internal var loginSessionPref: LoginSessionPref? = null
    private val refreshIcon: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_table)
        attendanceDB = AttendanceDB(this)
        detailAttendanceDB = DetailAttendanceDB(this)
        loginSessionPref = LoginSessionPref(this)

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
            supportActionBar!!.subtitle = "BETA"
        } catch (e: Exception) {
            try {
                actionBar!!.subtitle = "BETA"
            } catch (e1: Exception) {

            }

        }

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        mViewPager = findViewById<ViewPager>(R.id.container) as ViewPager
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timetable_menu, menu);
        return true;
    }

    */
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

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View {
            val rootView = inflater!!.inflate(R.layout.fragment_time_table, container, false)

            Log.e("onCreateView", "day : " + arguments!!.getString(ARG_DAY)!!)
            recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view) as RecyclerView
            recyclerView!!.addItemDecoration(DividerItemDecoration(context as Context, LinearLayoutManager.VERTICAL))
            recyclerView!!.setHasFixedSize(false)
            val attendanceList = computeTimeTable(arguments!!.getString(ARG_DAY))

            attendanceAdapter = TimetableAdapter(context as Context, attendanceList, object : TimetableAdapter.OnItemClickListener {
                override fun onItemClick(item: AttendanceResponseSingle) {

                }

                override fun onInfoButtonClick(item: AttendanceResponseSingle) {
                    Toast.makeText(context, "remove", Toast.LENGTH_SHORT).show()
                    Log.e("onRemoveButtonClick", "subjectCode : " + item.code!!)
                    Log.e("onRemoveButtonClick", "subjectName : " + item.name!!)
                    Log.e("onRemoveButtonClick", "day : " + arguments!!.getString(ARG_DAY)!!)
                    Log.e("onRemoveButtonClick", "-=-=-=-=-==-=-=-=-=-=-=-=-=-")

                    /*
                    int i = 0;
                    int pos = 0;
                    for(AttendanceResponseSingle att : attendanceList){
                        String _c = item.getCode();
                        String c[] = _c.split(" ");
                        String code = c[0].trim();
                        Log.e("onRemoveButtonClick","subjectCode : "+code+" "+att.getCode());
                        if(att.getCode().contains(code)){
                            pos = i;
                            Log.e("onRemoveButtonClick","MATCH at pos : "+pos);
                            att.setCode(code);
                            attendanceList.remove(att);
                            break;
                        }
                        i++;
                    }
                    Log.e("onRemoveButtonClick","attendanceList.size : "+attendanceList.size());
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    });
                    */
                    /*
                    Log.e("onRemoveButtonClick","day : "+getArguments().getString(ARG_DAY));
                    Log.e("onRemoveButtonClick","attendanceList.size : "+attendanceList.size());
                    int i = 0;
                    int pos = 0;
                    for(AttendanceResponseSingle att : attendanceList){
                        Log.e("onRemoveButtonClick",""+att.getCode()+" "+item.getCode());
                        if(att.getCode().equals(item.getCode())){
                            pos = i;
                        }
                        i++;
                    }
                    Log.e("onRemoveButtonClick","Object to remove at : "+pos);
                    attendanceList.remove(pos);
                    Log.e("onRemoveButtonClick","attendanceList.size : "+attendanceList.size());
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    });
                    */
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
        internal var days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

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


        fun computeTimeTable(day: String?): List<AttendanceResponseSingle> {
            val attendanceList = ArrayList<AttendanceResponseSingle>()
            //String days[] = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
            //for(String day : days) {

            val timetableList = ArrayList<Timetable>()

            val timeList = detailAttendanceDB!!.getAllTimeByDay(day!!)
            for (timee in timeList) {
                val _t = timee.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val time = _t[0]
                val subjectCode = _t[1]
                val frequency = detailAttendanceDB!!.countFrequency(subjectCode, day, time)
                Log.e("computeTimeTable", "$subjectCode | $time | $frequency")

                if (frequency > 4) {
                    val tT = Timetable(day, subjectCode, time)
                    timetableList.add(tT)
                    Log.e("computeTimeTable", "" + tT.toString())
                }
            }

            Collections.sort(timetableList) { o1, o2 ->
                val myTime = o1.time
                val _myAMPM = myTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val _myTime = myTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val _myampm = _myAMPM[1]
                var myT = Integer.valueOf(_myTime[0])!!
                if (_myampm == "pm" && myT != 12) {
                    myT = myT + 12
                }

                val compareTime = o2.time
                val _compareAMPM = compareTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val _compareTime = compareTime.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val _compareampm = _compareAMPM[1]
                var compareT = Integer.valueOf(_compareTime[0])!!
                if (_compareampm == "pm" && compareT != 12) {
                    compareT = compareT + 12
                }
                myT.compareTo(compareT)
            }

            var attendanceListTotal = attendanceDB!!.allAttendance

            Log.e("computeTimeTable", "TimeTable for : " + day)
            for (tT in timetableList) {
                Log.e("Timetable tT", "" + tT.toString())
                val sC = tT.subjectCode
                val time = tT.time
                for (attSingle in attendanceListTotal) {
                    if (attSingle.code == sC) {
                        attSingle.code = attSingle.code + " - " + time
                        attendanceList.add(attSingle)
                        attendanceListTotal = attendanceDB!!.allAttendance
                    }
                }
            }
            // }
            return attendanceList
        }
    }

}