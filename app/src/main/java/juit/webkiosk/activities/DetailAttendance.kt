package juit.webkiosk.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import java.util.ArrayList
import java.util.Collections

import juit.webkiosk.R
import juit.webkiosk.adapter.DetailAttendanceAdapter
import juit.webkiosk.database.AttendanceDB
import juit.webkiosk.database.DetailAttendanceDB
import juit.webkiosk.model.DetailAttendanceResponseSingle

/**
 * Created by puneet on 06/10/17.
 */

class DetailAttendance : AppCompatActivity() {
    internal var attendanceDB: AttendanceDB? = null
    internal var detailAttendanceDB: DetailAttendanceDB? = null
    var detailAttendanceList: List<DetailAttendanceResponseSingle> = ArrayList()
    internal var detailAttendanceAdapter: DetailAttendanceAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_attendance)

        var subjectCode: String? = ""
        var subjectName: String? = ""
        val i = intent
        try {
            val extras = i.extras
            subjectCode = extras.getString("subjectCode")
            subjectName = extras.getString("subjectName")
        } catch (e: Exception) {
            finish()
        }

        try {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        } catch (e: Exception) {
            try {
                actionBar!!.setDisplayHomeAsUpEnabled(true)
            } catch (e1: Exception) {

            }

        }

        title = subjectName
        attendanceDB = AttendanceDB(this)
        detailAttendanceDB = DetailAttendanceDB(this)
        detailAttendanceList = detailAttendanceDB!!.getAllAttendance(subjectCode!!)
        Collections.reverse(detailAttendanceList)
        val lview = findViewById<ListView>(R.id.listview)
        detailAttendanceAdapter = DetailAttendanceAdapter(this, detailAttendanceList)
        lview.adapter = detailAttendanceAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
