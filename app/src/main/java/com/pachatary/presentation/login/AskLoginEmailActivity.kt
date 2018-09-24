package com.pachatary.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import javax.inject.Inject

class AskLoginEmailActivity : AppCompatActivity(), AskLoginEmailView {

    private lateinit var progressBar: ProgressBar
    private lateinit var emailEditText: EditText
    private lateinit var askButton: Button
    private lateinit var rootView: CoordinatorLayout

    @Inject
    lateinit var presenter: AskLoginEmailPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, AskLoginEmailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_login_email)
        ToolbarUtils.setUp(this, title.toString(), true)

        rootView = findViewById(R.id.root)
        progressBar = findViewById(R.id.ask_login_email_progressbar)
        emailEditText = findViewById(R.id.ask_login_email_edittext)
        askButton = findViewById(R.id.ask_login_email_button)
        askButton.setOnClickListener { presenter.onAskClick(emailEditText.text.toString()) }

        PachataryApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)
    }

    override fun disableAskButton() {
        askButton.isEnabled = false
    }

    override fun enableAskButton() {
        askButton.isEnabled = true
    }

    override fun showSuccessMessage() {
        SnackbarUtils.showSuccess(rootView, this, getString(R.string.activity_ask_login_email_message))
    }

    override fun showErrorMessage() {
        SnackbarUtils.showError(rootView, this)
    }

    override fun showEmptyEmailError() {
        SnackbarUtils.showError(rootView, this,
                                getString(R.string.activity_ask_login_email_empty_email_message))
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun finishApplication() {
        Handler().postDelayed({ ActivityCompat.finishAffinity(this) }, 2000)
    }
}
