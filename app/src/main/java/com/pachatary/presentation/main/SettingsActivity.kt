package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.ToolbarUtils
import com.pachatary.presentation.login.AskLoginEmailActivity
import kotlinx.android.synthetic.main.activity_settings.*
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ToolbarUtils.setUp(this, title.toString(), true)

        findViewById<RelativeLayout>(R.id.settings_privacy_button)
            .setOnClickListener { startActivity(WebViewActivity.newPrivacyPolicyIntent(this)) }
        findViewById<RelativeLayout>(R.id.settings_terms_button)
            .setOnClickListener { startActivity(WebViewActivity.newTermsAndConditionsIntent(this)) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
