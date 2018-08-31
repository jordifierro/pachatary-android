package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import com.pachatary.BuildConfig
import com.pachatary.R
import com.pachatary.presentation.common.view.ToolbarUtils

class WebViewActivity : AppCompatActivity() {

    companion object {
        enum class WebViewType { PRIVACY_POLICY, TERMS_AND_CONDITIONS }

        const val WEBVIEWTYPE = "webview_type"

        fun newPrivacyPolicyIntent(context: Context): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(WEBVIEWTYPE, WebViewType.PRIVACY_POLICY)
            return intent
        }

        fun newTermsAndConditionsIntent(context: Context): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(WEBVIEWTYPE, WebViewType.TERMS_AND_CONDITIONS)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val type = intent.getSerializableExtra(WEBVIEWTYPE)

        if (type == WebViewType.PRIVACY_POLICY)
            ToolbarUtils.setUp(this, getString(R.string.activity_webview_privacy_title), true)
        else ToolbarUtils.setUp(this, getString(R.string.activity_webview_terms_title), true)

        val path = if (type == WebViewType.PRIVACY_POLICY) "/privacy-policy"
                   else "/terms-and-conditions"
        findViewById<WebView>(R.id.webview_webview).loadUrl(BuildConfig.API_URL + path)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
