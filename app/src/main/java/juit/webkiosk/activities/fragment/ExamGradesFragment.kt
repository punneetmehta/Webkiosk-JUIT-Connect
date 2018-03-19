package juit.webkiosk.activities.fragment

/**
 * Created by puneet on 03/11/17.
 */

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import droidmentor.helper.Retrofit.APIListener

import juit.webkiosk.R
import juit.webkiosk.helper.JUITKioskAPI
import juit.webkiosk.model.*
import juit.webkiosk.preference.LoginSessionPref
import retrofit2.Response
import android.widget.ArrayAdapter



class ExamGradesFragment : Fragment() {
    internal lateinit var semList: Spinner
    internal lateinit var swipeContainer: SwipeRefreshLayout
    internal lateinit var listView: ListView
    internal var loginSessionPref: LoginSessionPref? = null
    internal var getSubjectFaculty = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSubTitle("")
        loginSessionPref = LoginSessionPref((activity as Activity))
    }

    override fun onResume() {
        super.onResume()
        val kioskAPI = JUITKioskAPI((activity as Activity), SEMListResponse::class.java, semList_listener)
        if(kioskAPI.isNetworkAvailable((activity as Activity).applicationContext) && kioskAPI.isInternetAvailable()){
            setSubTitle("Getting SEM List")
            startRefersh()
        } else {
            stopRefresh();
        }
        kioskAPI.getSEMList(loginSessionPref!!.sessionKey, "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_exam_grades, container, false)
        semList = view!!.findViewById<Spinner>(R.id.semList)
        listView = view!!.findViewById<ListView>(R.id.listview)
        listView.setOnScrollListener(object : AbsListView.OnScrollListener{
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

            }

            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if(listView.getChildAt(0)!=null){
                    swipeContainer.isEnabled = listView.firstVisiblePosition == 0 && listView.getChildAt(0).top == 0
                }
            }

        })
        swipeContainer = view!!.findViewById<SwipeRefreshLayout>(R.id.swipeContainer)
        swipeContainer!!.setOnRefreshListener {
            val kioskAPI = JUITKioskAPI((activity as Activity), SEMListResponse::class.java, semList_listener)
            if(kioskAPI.isNetworkAvailable((activity as Activity).applicationContext) && kioskAPI.isInternetAvailable()){
                setSubTitle("Getting SEM List")
                startRefersh()
            } else {
                stopRefresh();
            }
            kioskAPI.getSEMList(loginSessionPref!!.sessionKey, "")
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

                if (getSubjectFaculty == 1) {
                    startRefersh();
                    setSubTitle("Getting SEM List")
                    val kioskAPI = JUITKioskAPI((activity as Activity), SEMListResponse::class.java, semList_listener)
                    kioskAPI.getSEMList(loginSessionPref!!.sessionKey, "")
                }
                return ;
            }
            Toast.makeText(activity, "Unable to Login, Please Logout and Login Again.", Toast.LENGTH_SHORT).show()
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText(activity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText(activity, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    internal var semList_listener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as SEMListResponse).error
            if (error.isEmpty() || error == null) {
                val SEM_List = res.semesters as List<SEMListResponseSingle>
                val spinnerArray = ArrayList<String>()
                for(sgcg in SEM_List){
                    spinnerArray.add(sgcg!!.code!!)
                }
                val spinnerArrayAdapter = ArrayAdapter<String>((activity as Activity).applicationContext, R.layout.spinner_item, spinnerArray)
                semList.adapter = spinnerArrayAdapter
                semList.invalidate();


                semList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                        startRefersh();
                        setSubTitle("Getting Exam Grades...")
                        val kioskAPI = JUITKioskAPI((activity as Activity), ExamGradesResponse::class.java, examGradeListner)
                        kioskAPI.getEXAMGrades(loginSessionPref!!.sessionKey, spinnerArray.get(pos), "")
                    }

                    override fun onNothingSelected(parent: AdapterView<out Adapter>?) {

                    }

                }

                stopRefresh();
                if(spinnerArray.get(0)!="") {
                    startRefersh();
                    setSubTitle("Getting Exam Grades...")
                    val kioskAPI = JUITKioskAPI((activity as Activity), ExamGradesResponse::class.java, examGradeListner)
                    kioskAPI.getEXAMGrades(loginSessionPref!!.sessionKey, spinnerArray.get(0), "")
                }
                //swipeContainer.isEnabled = false
                return ;
            }
            getSubjectFaculty = 1
            setSubTitle("Updating Login Token...")
            val kioskAPI = JUITKioskAPI((activity as Activity), LoginResponse::class.java, loginListener)
            kioskAPI.processLogin(loginSessionPref!!.username,loginSessionPref!!.password, "")
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText(activity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText(activity, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    internal var examGradeListner: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as ExamGradesResponse).error
            if (error.isEmpty() || error == null) {
                val Grades_List = res.grades as List<ExamGradesResponseSingle>
                val spinnerArray = ArrayList<String>()
                stopRefresh()
                listView.adapter = CustomListAdapter((activity as Activity), Grades_List)
                listView.invalidate();
                return ;
            }
            getSubjectFaculty = 1
            setSubTitle("Updating Login Token...")
            val kioskAPI = JUITKioskAPI((activity as Activity), LoginResponse::class.java, loginListener)
            kioskAPI.processLogin(loginSessionPref!!.username,loginSessionPref!!.password, "")
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText(activity, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText(activity, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    class CustomListAdapter(activity : Activity, customList: List<ExamGradesResponseSingle>) : BaseAdapter(){

        var activity: Activity = activity
        var customList: List<ExamGradesResponseSingle> = customList
        var inflater:LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;

        override fun getCount(): Int {
            return customList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): juit.webkiosk.model.ExamGradesResponseSingle {
            return customList.get(position)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.exam_grades_row, null)
            }

            val subject = view!!.findViewById<TextView>(R.id.subject)
            val grade = view.findViewById<TextView>(R.id.grade)

            var gradeData = customList.get(position)

            subject.setText(gradeData.subject)
            grade.setText(gradeData.grade)

            return view
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {

        fun newInstance(title: String): ExamGradesFragment {
            val fragment = ExamGradesFragment()
            val args = Bundle()
            //args.putString(KEY_MOVIE_TITLE, movieTitle);
            fragment.arguments = args
            return fragment
        }
    }
}

