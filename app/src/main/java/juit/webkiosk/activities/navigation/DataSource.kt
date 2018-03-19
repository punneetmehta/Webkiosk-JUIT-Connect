package juit.webkiosk.activities.navigation

import android.content.Context

import java.util.ArrayList
import java.util.Arrays
import java.util.TreeMap

/**
 * Created by puneet on 03/11/17.
 */

object DataSource {

    fun getData(context: Context): Map<String, List<String>> {
        val expandableListData = TreeMap<String, List<String>>()

        val personalInfo = ArrayList<String>()
        personalInfo.add("Personal Details")

        val academiclInfo = ArrayList<String>()
        academiclInfo.add("My Attendance")
        academiclInfo.add("Subject Regtd.")
        academiclInfo.add("Subject Faculty")

        val examInfo = ArrayList<String>()
        examInfo.add("Exam Grades")
        examInfo.add("View SGPA/CGPA")

        expandableListData.put("Personal Info", personalInfo)
        expandableListData.put("Academic Info", academiclInfo)
        expandableListData.put("Exam Info", examInfo)

        return expandableListData
    }

}
