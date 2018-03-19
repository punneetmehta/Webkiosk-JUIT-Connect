package juit.webkiosk.activities.navigation

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import juit.webkiosk.R
import juit.webkiosk.activities.MainActivity
import juit.webkiosk.activities.fragment.*

/**
 * Created by puneet on 03/11/17.
 */

class FragmentNavigationManager : NavigationManager {

    private var mFragmentManager: FragmentManager? = null
    private var mActivity: MainActivity? = null

    private fun configure(activity: MainActivity) {
        mActivity = activity
        mFragmentManager = mActivity!!.supportFragmentManager
    }

    override fun showFragmentAttendance(title: String) {
        showFragment(AttendanceFragment.newInstance(title), false)
    }

    override fun showFragmentExamGrades(title: String) {
        showFragment(ExamGradesFragment.newInstance(title), false)
    }

    override fun showFragmentPersonalDetails(title: String) {
        showFragment(PersonalDetailsFragment.newInstance(title), false)
    }

    override fun showFragmentSGPACGPA(title: String) {
        showFragment(SGPA_CGPA_Fragment.newInstance(title), false)
    }

    override fun showFragmentSubjectFaculty(title: String) {
        showFragment(SubjectFacultyFragment.newInstance(title), false)
    }

    override fun showFragmentSubjectsRegtd(title: String) {
        showFragment(SubjectRegtdFragment.newInstance(title), false)
    }

    private fun showFragment(fragment: Fragment, allowStateLoss: Boolean) {
        val fm = mFragmentManager

        @SuppressLint("CommitTransaction")
        val ft = fm!!.beginTransaction().replace(R.id.container, fragment)

        ft.addToBackStack(null)
        ft.commitAllowingStateLoss()
        fm.executePendingTransactions()
    }

    companion object {

        private var sInstance: FragmentNavigationManager? = null

        fun obtain(activity: MainActivity): FragmentNavigationManager {
            if (sInstance == null) {
                sInstance = FragmentNavigationManager()
            }
            sInstance!!.configure(activity)
            return sInstance as FragmentNavigationManager
        }
    }
}