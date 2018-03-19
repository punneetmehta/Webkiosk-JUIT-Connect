package juit.webkiosk.activities.fragment

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

/**
 * Created by puneet on 03/11/17.
 */

class SGPA_CGPA_Fragment : Fragment() {

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
        val kioskAPI = JUITKioskAPI((activity as Activity), CG_SG_Response::class.java, sgpa_cgpa_listener)
        if(kioskAPI.isNetworkAvailable((activity as Activity).applicationContext) && kioskAPI.isInternetAvailable()){
            setSubTitle("Getting SGPA/CGPA")
            startRefersh()
        } else {
            stopRefresh();
        }
        kioskAPI.getSGPACGPA(loginSessionPref!!.sessionKey, "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_sgpa_cgpa, container, false)
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
            val kioskAPI = JUITKioskAPI((activity as Activity), CG_SG_Response::class.java, sgpa_cgpa_listener)
            if(kioskAPI.isNetworkAvailable((activity as Activity).applicationContext) && kioskAPI.isInternetAvailable()){
                setSubTitle("Getting SGPA/CGPA")
                startRefersh()
            } else {
                stopRefresh();
            }
            kioskAPI.getSGPACGPA(loginSessionPref!!.sessionKey, "")
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
                    setSubTitle("Getting SGPA/CGPA")
                    startRefersh();
                    val kioskAPI = JUITKioskAPI((activity as Activity), CG_SG_Response::class.java, sgpa_cgpa_listener)
                    kioskAPI.getSGPACGPA(loginSessionPref!!.sessionKey, "")
                    getSubjectFaculty = 0
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

    internal var sgpa_cgpa_listener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as CG_SG_Response).error
            if (error.isEmpty() || error == null) {
                val SGPACGPA_List = res.cgsg as List<CG_SG_ResponseSingle>
                stopRefresh()
                listView.adapter = CustomListAdapter((activity as Activity), SGPACGPA_List)
                listView.invalidate();
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

    class CustomListAdapter(activity : Activity, customList: List<CG_SG_ResponseSingle>) : BaseAdapter(){

        var activity: Activity = activity
        var customList: List<CG_SG_ResponseSingle> = customList
        var inflater:LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;

        override fun getCount(): Int {
            return customList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): juit.webkiosk.model.CG_SG_ResponseSingle {
            return customList.get(position)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.sgpa_cgpa_row, null)
            }

            val sem = view!!.findViewById<TextView>(R.id.sem)
            val cgpa = view.findViewById<TextView>(R.id.cgpa)
            val sgpa = view.findViewById<TextView>(R.id.sgpa)

            var semData = customList.get(position)

            sem.setText(semData.id)
            cgpa.setText(semData.cgpa)
            sgpa.setText(semData.sgpa)

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

        fun newInstance(title: String): SGPA_CGPA_Fragment {
            val fragment = SGPA_CGPA_Fragment()
            val args = Bundle()
            //args.putString(KEY_MOVIE_TITLE, movieTitle);
            fragment.arguments = args
            return fragment
        }
    }
}