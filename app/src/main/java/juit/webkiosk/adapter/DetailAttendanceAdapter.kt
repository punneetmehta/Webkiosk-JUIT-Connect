package juit.webkiosk.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import java.util.ArrayList

import juit.webkiosk.R
import juit.webkiosk.model.DetailAttendanceResponseSingle

/**
 * Created by puneet on 06/10/17.
 */

class DetailAttendanceAdapter(internal var activity: Activity, var detailAttendanceList: List<DetailAttendanceResponseSingle>) : BaseAdapter() {

    override fun getCount(): Int {
        return detailAttendanceList.size
    }

    override fun getItem(position: Int): Any {
        return detailAttendanceList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private inner class ViewHolder {
        internal var status: TextView? = null
        internal var faculty: TextView? = null
        internal var date: TextView? = null
        internal var time: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        val holder: ViewHolder
        val inflater = activity.layoutInflater

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_detail_attendance_row, null)
            holder = ViewHolder()
            holder.status = convertView!!.findViewById<TextView>(R.id.status) as TextView
            holder.faculty = convertView.findViewById<TextView>(R.id.faculty) as TextView
            holder.date = convertView.findViewById<TextView>(R.id.date) as TextView
            holder.time = convertView.findViewById<TextView>(R.id.time) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val item = detailAttendanceList[position]
        //holder.status.setText(item.getsNo().toString());
        if (item.status == "Present") {
            // Green
            holder.status!!.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            // Red
            holder.status!!.setBackgroundColor(Color.parseColor("#f44336"))
        }
        holder.faculty!!.text = item.faculty!!.toString()
        holder.date!!.text = item.date!!.toString()
        holder.time!!.text = item.time!!.toString()

        return convertView
    }
}

