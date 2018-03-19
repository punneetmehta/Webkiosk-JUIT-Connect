package juit.webkiosk.activities.navigation

/**
 * Created by puneet on 03/11/17.
 */

interface NavigationManager {

    fun showFragmentAttendance(title: String)
    fun showFragmentExamGrades(title: String)
    fun showFragmentPersonalDetails(title: String)
    fun showFragmentSGPACGPA(title: String)
    fun showFragmentSubjectFaculty(title: String)
    fun showFragmentSubjectsRegtd(title: String)
}