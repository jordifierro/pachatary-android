package com.pachatary.presentation.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import javax.inject.Inject


class RegisterActivity : AppCompatActivity(), RegisterView {

    lateinit var progressBar: ProgressBar
    lateinit var usernameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var doneButton: Button
    lateinit var rootView: CoordinatorLayout

    @Inject
    lateinit var presenter: RegisterPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ToolbarUtils.setUp(this, getString(R.string.title_register_activity), true)

        rootView = findViewById(R.id.root)
        progressBar = findViewById(R.id.register_progressbar)
        usernameEditText = findViewById(R.id.register_edit_username_edittext)
        emailEditText = findViewById(R.id.register_edit_email_edittext)
        doneButton = findViewById(R.id.register_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }

        PachataryApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)
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

    override fun blockDoneButton(block: Boolean) {
        doneButton.isEnabled = !block
    }

    override fun getUsername() = usernameEditText.editableText.toString()
    override fun getEmail() = emailEditText.editableText.toString()

    override fun showSuccessMessage() {
        SnackbarUtils.showSuccess(rootView, this, getString(R.string.register_success_message))
    }

    override fun showErrorMessage(message: String) {
        SnackbarUtils.showError(rootView, this, message)
    }

    override fun finishApplication() {
        Handler().postDelayed({ ActivityCompat.finishAffinity(this) }, 2000)
    }
}
