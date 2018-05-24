package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.login.AskLoginEmailActivity
import javax.inject.Inject

class WelcomeActivity : AppCompatActivity(), WelcomeView {

    private lateinit var progressBar: ProgressBar
    private lateinit var startButton: Button
    private lateinit var loginButton: Button

    @Inject
    lateinit var presenter: WelcomePresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, WelcomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        progressBar = findViewById(R.id.welcome_progressbar)
        startButton = findViewById(R.id.welcome_start_button)
        startButton.setOnClickListener { presenter.onStartClick() }
        loginButton = findViewById(R.id.welcome_login_button)
        loginButton.setOnClickListener { presenter.onLoginClick() }
        findViewById<TextView>(R.id.welcome_legal2_textview)
                .setOnClickListener { presenter.onPrivacyPolicyClick() }
        findViewById<TextView>(R.id.welcome_legal4_textview)
                .setOnClickListener { presenter.onTermsAndConditionsClick() }

        PachataryApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)
    }

    override fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun navigateToAskLogin() {
        startActivity(AskLoginEmailActivity.newIntent(this))
    }

    override fun disableButtons() {
        startButton.isEnabled = false
        loginButton.isEnabled = false
    }

    override fun enableButtons() {
        startButton.isEnabled = true
        loginButton.isEnabled = true
    }

    override fun showErrorMessage() {
        Toast.makeText(this, "Some error has occurred", Toast.LENGTH_SHORT).show()
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun navigateToPrivacyPolicy() {
        startActivity(WebViewActivity.newPrivacyPolicyIntent(this))
    }

    override fun navigateToTermsAndConditions() {
        startActivity(WebViewActivity.newTermsAndConditionsIntent(this))
    }
}
