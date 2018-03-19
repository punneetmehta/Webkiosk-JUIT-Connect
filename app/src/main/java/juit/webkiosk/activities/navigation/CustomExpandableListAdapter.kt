package juit.webkiosk.activities.navigation

/**
 * Created by puneet on 03/11/17.
 */

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import juit.webkiosk.R

class CustomExpandableListAdapter(private val mContext: Context, private val mExpandableListTitle: List<String>, private val mExpandableListDetail: Map<String, List<String>>) : BaseExpandableListAdapter() {
    private val mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return mExpandableListDetail[mExpandableListTitle[listPosition]]!!.get(expandedListPosition)
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int,
                              isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.navigation_list_item, null)
        }
        val expandedListTextView = convertView!!.findViewById<TextView>(R.id.expandedListItem)
        expandedListTextView.text = expandedListText
        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return mExpandableListDetail[mExpandableListTitle[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return mExpandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return mExpandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.navigation_list_group, null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
