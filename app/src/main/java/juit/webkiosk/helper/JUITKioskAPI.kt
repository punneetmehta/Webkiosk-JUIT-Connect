package juit.webkiosk.helper

import android.app.Activity
import android.content.Context
import java.util.HashMap

import droidmentor.helper.Retrofit.APICall
import droidmentor.helper.Retrofit.APIListener
import droidmentor.helper.Retrofit.ApiClient
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.content.Context.CONNECTIVITY_SERVICE
import android.widget.Toast
import java.net.InetAddress
import java.net.UnknownHostException


/**
 * Created by puneet on 05/10/17.
 */

class JUITKioskAPI<T>(internal var mContext: Activity, internal var responseModel: Class<T>, internal var apiListener: APIListener) {
    internal var networkCall: APICall<T>

    internal var APIHost = "12s51emmid.execute-api.ap-south-1.amazonaws.com"
    internal var APIBase = "https://"+APIHost+"/v1/"
    internal var loginURL = "JUITKiosk_Login"
    internal var userDetailsURL = "JUITKiosk_UserDetails"
    internal var attendanceURL = "JUITKiosk_Attendance"
    internal var detailAttendanceURL = "JUITKiosk_DetailAttendance"
    internal var subjectRegtd = "JUITKiosk_Academic_RegisteredSubjects"
    internal var subjectFaculty = "JUITKiosk_Academics_SubjectFaculty"
    internal var SGPA_CGPA = "JUITKiosk_ExamInfo_SGPA_CGPA"
    internal var SEM_LIST = "JUITKiosk_ExamInfo_GetSemesters"
    internal var EXAM_GRADES = "JUITKiosk_ExamInfo_GetGradesBySem"

    internal var JUITKiosk_API_KEY = "API_KEY"; // For Geting API Key, drop me a Mail at i@puneet.cc

    init {
        ApiClient.setBaseUrl(APIBase)
        //ApiClient.setNetworkErrorMessage("No Internet Connection")
        ApiClient.showNetworkErrorMessage(false)
        val default_headers = HashMap<String, String>()
        default_headers.put("x-api-key", JUITKiosk_API_KEY)
        ApiClient.setCommonHeaders(default_headers)

        networkCall = APICall(mContext)
    }

    fun processLogin(uid: String, pwd: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("uid", uid)
        queryParams.put("pwd", pwd)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, loginURL, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getUserDetails(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, userDetailsURL, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getAttendance(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, attendanceURL, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getDetailAttendance(session: String, subjectCode: String, data: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        queryParams.put("data", data)
        queryParams.put("code", subjectCode)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, detailAttendanceURL, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getSubjectRegtd(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, subjectRegtd, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getSubjectFaculty(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, subjectFaculty, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getSGPACGPA(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, SGPA_CGPA, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getSEMList(session: String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, SEM_LIST, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun getEXAMGrades(session: String,examCode : String, loadingMessage: String) {
        if(!isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            return ;
        }
        if(!isInternetAvailable()){
            Toast.makeText(mContext,"API Server Not Reachable, Please Check Your Internet Connectivity",Toast.LENGTH_SHORT).show()
            return ;
        }
        var showLoader = true
        val queryParams = HashMap<String, String>()
        queryParams.put("session", session)
        queryParams.put("sem", examCode)
        if (loadingMessage.length==0) {
            showLoader = false
        }
        networkCall.APIRequest(APICall.Method.GET, EXAM_GRADES, responseModel, null, queryParams, apiListener, showLoader, loadingMessage)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }

    fun isInternetAvailable(): Boolean {
        return true;
        var isInternetAvailable:Boolean = false;

        var th = Thread(Runnable {
            try {
                val address = InetAddress.getByName(APIHost)
                isInternetAvailable = !address.equals("")
            } catch (e: UnknownHostException) {
                // Log error
            }
        });
        th.start();
        th.join();

        return isInternetAvailable
    }



}
