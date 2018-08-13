package com.pachatary.presentation.common.view

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.pachatary.R


class ToolbarUtils {
    companion object {
        fun setUp(appCompatActivity: AppCompatActivity, title: String, backEnabled: Boolean) {
            val toolbar = appCompatActivity.findViewById<Toolbar>(R.id.toolbar)
            appCompatActivity.setSupportActionBar(toolbar)
            appCompatActivity.supportActionBar!!.setHomeButtonEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.title = ""
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left)
            toolbar.findViewById<TextView>(R.id.toolbar_title).text = title
        }
    }
}