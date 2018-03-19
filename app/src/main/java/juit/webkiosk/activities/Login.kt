package juit.webkiosk.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.ArrayList
import droidmentor.helper.Retrofit.APIListener
import juit.webkiosk.R
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.database.UserDetailsDB
import juit.webkiosk.helper.JUITKioskAPI
import juit.webkiosk.model.AttendanceResponse
import juit.webkiosk.model.LoginResponse
import juit.webkiosk.model.UserDetailsResponse
import juit.webkiosk.preference.LoginSessionPref
import juit.webkiosk.preference.TimeTablePref
import juit.webkiosk.preference.firstTimeLaunch
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class Login : AppCompatActivity() {
    internal var userDetailsDB: UserDetailsDB? = null
    internal var attendanceDB: AttendanceDB? = null
    internal var detailAttendanceDB: DetailAttendanceDB? = null

    internal var loginSessionPref: LoginSessionPref? = null
    internal var timeTablePref: TimeTablePref? = null

    internal lateinit var username: String
    internal lateinit var password: String
    internal var progressDialog: ProgressDialog? = null
    internal var activity: Activity? = this

    internal var fTL: firstTimeLaunch? = null
    internal var SHOWCASE_ID = "loginActivity_showCase"

    internal var uid: EditText? = null
    internal var pwd: EditText? = null
    internal var batch: EditText? = null
    internal var login: Button? = null
    private var batchName:String = "";
    internal var loginListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val loginResult = (res as LoginResponse).loginResult
            val loginResponse = res.response

            if (loginResult == 1) {
                val loginCookies = res.loginCookies
                if (loginSessionPref!!.username.isEmpty() || loginSessionPref!!.password.isEmpty() || loginSessionPref!!.sessionKey.isEmpty()) {
                    timeTablePref!!.setBatch(batchName)
                    getTimeTable(batchName);
                    progressDialog!!.setMessage("Setting Up...\nIt may take upto 15-20 Seconds...")
                    progressDialog!!.show()
                    var kAPI = JUITKioskAPI(this@Login, UserDetailsResponse::class.java, userDetailsListner)
                    kAPI.getUserDetails(loginCookies!!, "")

                    var kAPI2 = JUITKioskAPI(this@Login, AttendanceResponse::class.java, attendanceListener)
                    kAPI2.getAttendance(loginCookies!!, "")

                } else {
                    loginSessionPref!!.sessionKey = loginCookies!!
                    loginSessionPref!!.username = username
                    loginSessionPref!!.password = password
                    val i = Intent(this@Login, MainActivity::class.java)
                    startActivity(i)
                    this@Login.finish()
                }
                loginSessionPref!!.sessionKey = loginCookies!!
                loginSessionPref!!.username = username
                loginSessionPref!!.password = password

            } else {
                Toast.makeText(this@Login, "Invalid Login Details...", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(from: Int, t: Throwable) {
            Toast.makeText(this@Login, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            Toast.makeText(this@Login, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTimeTable(batchName:String){
        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted())
                        {
                            createJUITDir();
                            DownloadFileFromURL().execute("https://s3.ap-south-1.amazonaws.com/juit-webkiosk/2018EVESEM/"+batchName+".json")
                        }
                        if (report.isAnyPermissionPermanentlyDenied())
                        {
                            showSettingsDialog()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions:List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener(object : PermissionRequestErrorListener {
            override fun onError(error: DexterError?) {
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()
            }

        }).onSameThread().check()

    }

    private fun createJUITDir() : Boolean{
        val folderPath = (Environment.getExternalStorageDirectory().toString())+"/JUIT_WebKiosk"
        val folder = File(folderPath)
        var success = true
        if (!folder.exists())
        {
            success = folder.mkdir()
        }
        return success
    }

    internal class DownloadFileFromURL: AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg f_url:String):String {

            var count:Int
            var output_file:String = "";
            try
            {
                val url = URL(f_url[0])
                val urlSplit = f_url[0].split("/")
                val fileName = urlSplit[urlSplit.size-1];
                val conection = url.openConnection()
                conection.connect()
                val lenghtOfFile = conection.getContentLength()
                val input = BufferedInputStream(url.openStream(), 8192)
                output_file = (Environment.getExternalStorageDirectory().toString()+"/JUIT_WebKiosk/")+fileName
                val output = FileOutputStream(output_file)
                val data = ByteArray(1024)
                var total:Long = 0
                count = input.read(data)
                while (count != -1)
                {
                    total += count.toLong()
                    output.write(data, 0, count)
                    count = input.read(data)
                }
                output.flush()
                output.close()
                input.close()

            }
            catch (e:Exception) {
                Log.e("Error: ", e.message)
            }
            return output_file;
        }

        override fun onPostExecute(file_url:String) {
            Log.e("onPostExecute: ", ""+file_url)
            var TimeTable = TimeTable()
            if(!file_url.isEmpty()) {
                //TimeTable.setUpView()
            }
            super.onPostExecute(file_url)
        }

    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@Login)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS", object: DialogInterface.OnClickListener {
            override  fun onClick(dialog: DialogInterface, which:Int) {
                dialog.cancel()
                openSettings()
            }
        })
        builder.setNegativeButton("Cancel", object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which:Int) {
                dialog.cancel()
            }
        })
        builder.show()
    }
    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", getPackageName(), null)
        intent.setData(uri)
        startActivityForResult(intent, 101)
    }

    internal var userDetailsListner: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val res = (res as UserDetailsResponse)
            val EnrNo = res.EnrNo!!

            if (userDetailsDB!!.checkIfExist(EnrNo) == 0) {
                userDetailsDB!!.addDetails(res)
            }
        }

        override fun onFailure(from: Int, t: Throwable) {
            progressDialog!!.dismiss()
            Toast.makeText(this@Login, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            progressDialog!!.dismiss()
            Toast.makeText(this@Login, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    internal var attendanceListener: APIListener = object : APIListener {
        override fun onSuccess(from: Int, response: Response<*>, res: Any) {
            val error = (res as AttendanceResponse).error
            if (error.isEmpty() || error == null) {
                val subjectCodeList = ArrayList<String>()
                var subjectCodeListInDB: ArrayList<String> = ArrayList()
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
                subjectCodeListInDB = attendanceDB!!.allSubjectCode as ArrayList<String>

                subjectCodeListInDB.removeAll(subjectCodeList)
                for (subjectCode in subjectCodeListInDB) {
                    // Delete them { in case of Change of Subjects, OLD Subjects Need to be Deleted }
                    attendanceDB!!.deleteDetails(subjectCode)
                }
                progressDialog!!.dismiss()
                val i = Intent(this@Login, MainActivity::class.java)
                startActivity(i)
                this@Login.finish()
            } else {
                progressDialog!!.dismiss()
            }
        }

        override fun onFailure(from: Int, t: Throwable) {
            progressDialog!!.dismiss()
            Toast.makeText(this@Login, "Server Not Responding...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }

        override fun onNetworkFailure(from: Int) {
            progressDialog!!.dismiss()
            Toast.makeText(this@Login, "Network Error...\nPlease Try Again Later...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fTL = firstTimeLaunch(this)
        loginSessionPref = LoginSessionPref(this)
        timeTablePref = TimeTablePref(this)
        if (!loginSessionPref!!.username.isEmpty() && !loginSessionPref!!.password.isEmpty() && !loginSessionPref!!.sessionKey.isEmpty()) {
            if(!timeTablePref!!.getBatch().equals("")){
                if(!File(timeTablePref!!.getTimeTableFile()).exists()){
                    getTimeTable(timeTablePref!!.getBatch());
                }
            }
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
            return
        }

        activity = this
        userDetailsDB = UserDetailsDB(this)
        attendanceDB = AttendanceDB(this)
        detailAttendanceDB = DetailAttendanceDB(this)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        uid = findViewById<EditText>(R.id.username)
        pwd = findViewById<EditText>(R.id.password)
        batch = findViewById<EditText>(R.id.batch)
        login = findViewById<Button>(R.id.login)

        uid!!.setText("" + loginSessionPref!!.username)
        pwd!!.setText("" + loginSessionPref!!.password)

        login!!.setOnClickListener(View.OnClickListener {

            if(Build.VERSION.SDK_INT >= 23){
                    Dexter.withActivity(this)
                            .withPermissions(
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(object: MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                    if (report.areAllPermissionsGranted())
                                    {
                                        username = uid!!.text.toString().trim { it <= ' ' }
                                        password = pwd!!.text.toString().trim { it <= ' ' }
                                        batchName = batch!!.text.toString().trim { it <= ' ' }
                                        batchName = batchName.capitalize();

                                        if (username.isEmpty() || username.length != 6) {
                                            uid!!.error = "Invalid Username"
                                            return
                                        }
                                        if (password.isEmpty()) {
                                            pwd!!.error = "Invalid Password"
                                            return
                                        }
                                        if (batchName.isEmpty()) {
                                            batch!!.error = "Invalid Batch"
                                            return
                                        }

                                        var kioskAPI = JUITKioskAPI(this@Login, LoginResponse::class.java, loginListener)
                                        kioskAPI.processLogin(username, password, "Logging In...")
                                    }
                                    if (report.isAnyPermissionPermanentlyDenied())
                                    {
                                        showSettingsDialog()
                                    }
                                }
                                override fun onPermissionRationaleShouldBeShown(permissions:List<PermissionRequest>, token: PermissionToken) {
                                    token.continuePermissionRequest()
                                }
                            }).withErrorListener(object : PermissionRequestErrorListener {
                        override fun onError(error: DexterError?) {
                            Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()
                        }

                    }).onSameThread().check()
            } else {
                username = uid!!.text.toString().trim { it <= ' ' }
                password = pwd!!.text.toString().trim { it <= ' ' }
                batchName = batch!!.text.toString().trim { it <= ' ' }
                batchName = batchName.capitalize();

                if (username.isEmpty() || username.length != 6) {
                    uid!!.error = "Invalid Username"
                } else {
                    if (password.isEmpty()) {
                        pwd!!.error = "Invalid Password"
                    } else {
                        if (batchName.isEmpty()) {
                            batch!!.error = "Invalid Batch"
                        } else {

                            var kioskAPI = JUITKioskAPI(this@Login, LoginResponse::class.java, loginListener)
                            kioskAPI.processLogin(username, password, "Logging In...")
                        }
                    }
                }
            }
        })


    }

}
