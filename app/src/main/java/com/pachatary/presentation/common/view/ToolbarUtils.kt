package com.pachatary.presentation.common.view

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.pachatary.R
import android.graphics.Typeface
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.View


class ToolbarUtils {
    companion object {
        fun setUp(appCompatActivity: AppCompatActivity, title: String, backEnabled: Boolean) {
            val toolbar = appCompatActivity.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left)
            toolbar.findViewById<TextView>(R.id.toolbar_title).text = title
            appCompatActivity.setSupportActionBar(toolbar)
            appCompatActivity.supportActionBar!!.setHomeButtonEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.title = ""
        }

        fun setUp(view: View, appCompatActivity: AppCompatActivity,
                  title: String, backEnabled: Boolean) {
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left)
            toolbar.findViewById<TextView>(R.id.toolbar_title).text = title
            appCompatActivity.setSupportActionBar(toolbar)
            appCompatActivity.supportActionBar!!.setHomeButtonEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(backEnabled)
            appCompatActivity.supportActionBar!!.title = ""
        }

        fun activateCustomFont(view: View, appCompatActivity: AppCompatActivity) {
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            val titleTextView = toolbar.findViewById<TextView>(R.id.toolbar_title)
            val font = Typeface.createFromAsset(appCompatActivity.assets,
                                                "fonts/bahiana_regular.ttf")
            titleTextView.typeface = font
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40.0f)
        }
    }
}