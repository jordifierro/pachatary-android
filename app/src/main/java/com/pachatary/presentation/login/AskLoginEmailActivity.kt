package com.pachatary.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import javax.inject.Inject

class AskLoginEmailActivity : AppCompatActivity(), AskLoginEmailView {

    private lateinit var progressBar: ProgressBar
    private lateinit var emailEditText: EditText
    private lateinit var askButton: Button

    @Inject
    lateinit var presenter: AskLoginEmailPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, AskLoginEmailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_login_email)

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
        Toast.makeText(this, "We've sent an email to you!", Toast.LENGTH_SHORT).show()
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
}
