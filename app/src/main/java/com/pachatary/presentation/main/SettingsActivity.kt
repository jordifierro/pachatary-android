package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import com.pachatary.BuildConfig
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
        findViewById<RelativeLayout>(R.id.feedback_button)
                .setOnClickListener {
                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", BuildConfig.FEEDBACK_EMAIL, null))
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN &&
                            android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(BuildConfig.FEEDBACK_EMAIL))
                    startActivity(Intent.createChooser(emailIntent, ""))}
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
