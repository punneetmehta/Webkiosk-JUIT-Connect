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
import juit.webkiosk.model.LoginResponse
import juit.webkiosk.model.UserDetailsResponse
import juit.webkiosk.preference.LoginSessionPref
import retrofit2.Response

/**
 * Created by puneet on 03/11/17.
 */

class PersonalDetailsFragment : Fragment() {

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
        var kAPI = JUITKioskAPI((activity as Activity), UserDetailsResponse::class.java, userDetailsListner)
        if(kAPI.isNetworkAvailable((activity as Activity).applicationContext) && kAPI.isInternetAvailable()){
            setSubTitle("Getting Personal Info...")
            startRefersh()
        } else {
            stopRefresh();
        }
        kAPI.getUserDetails(loginSessionPref!!.sessionKey, "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_personal_info, container, false)
        listView = view!!.findViewById<ListView>(R.id.list_view)
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
            var kAPI = JUITKioskAPI((activity as Activity), UserDetailsResponse::class.java, userDetailsListner)
            if(kAPI.isNetworkAvailable((activity as Activity).applicationContext) && kAPI.isInternetAvailable()){
                setSubTitle("Getting Personal Info...")
                startRefersh()
            } else {
                stopRefresh();
            }
            kAPI.getUserDetails(loginSessionPref!!.sessionKey, "")
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
                    setSubTitle("Getting Personal Info...")
                    var kAPI = JUITKioskAPI((activity as Activity), UserDetailsResponse::class.java, userDetailsListner)
                    kAPI.getUserDetails(loginSessionPref!!.sessionKey, "")
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

    internal var userDetailsListner: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val res = (res as UserDetailsResponse)
            val error = res.error
            if (error.isEmpty() || error == null) {
                val ParentMobile = res.ParentMobile
                val name = res.name!!
                val StudentEmail = res.StudentEmail!!
                val StudentMobile = res.StudentMobile!!
                val Course = res.Course!!
                val ParentEmail = res.ParentEmail!!
                val FatherName = res.FatherName!!
                val EnrNo = res.EnrNo!!

                val userDetails = LinkedHashMap<String, String>()
                userDetails.put("EnrNo", "" + EnrNo);
                userDetails.put("Name", "" + name);
                userDetails.put("Father Name", "" + FatherName);
                userDetails.put("Course", "" + Course);
                userDetails.put("Student Email", "" + StudentEmail);
                userDetails.put("Student Mobile", "" + StudentMobile);
                userDetails.put("Parent Email", "" + ParentEmail);
                userDetails.put("Parent Mobile", "" + ParentMobile);
                listView.adapter = CustomListAdapter((activity as Activity), userDetails)
                listView.invalidate();
                stopRefresh();
                return ;
            }
            getSubjectFaculty = 1
            setSubTitle("Updating Login Token...")
            val kioskAPI = JUITKioskAPI((activity as Activity), LoginResponse::class.java, loginListener)
            kioskAPI.processLogin(loginSessionPref!!.username,loginSessionPref!!.password, "")
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText((activity as Activity).applicationContext, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText((activity as Activity).applicationContext, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    class CustomListAdapter(activity : Activity, customList: LinkedHashMap<String, String>) : BaseAdapter(){

        var activity: Activity = activity
        var customList: Map<String, String> = customList
        var inflater:LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;

        override fun getCount(): Int {
            return customList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Map.Entry<String, String> {
            return customList.entries.elementAt(position)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.personal_details_row, null)
            }

            val key = view!!.findViewById<TextView>(R.id.key)
            val value = view.findViewById<TextView>(R.id.value)

            var item = getItem(position)

            key.setText(item.key)
            if(item.value.isNullOrEmpty() || item.value.equals("null")) {
                value.setText("Not Provided")
            } else {
                value.setText(item.value)
            }
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

        fun newInstance(title: String): PersonalDetailsFragment {
            val fragment = PersonalDetailsFragment()
            val args = Bundle()
            //args.putString(KEY_MOVIE_TITLE, movieTitle);
            fragment.arguments = args
            return fragment
        }
    }
}
