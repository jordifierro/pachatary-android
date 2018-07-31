package com.pachatary.presentation.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import kotlinx.android.synthetic.main.activity_register.*
import javax.inject.Inject
import android.widget.Toast


class RegisterActivity : AppCompatActivity(), RegisterView {

    lateinit var progressBar: ProgressBar
    lateinit var usernameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var doneButton: Button

    @Inject
    lateinit var presenter: RegisterPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)

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

    override fun blockDoneButton(block: Boolean) {
        doneButton.isEnabled = !block
    }

    override fun getUsername() = usernameEditText.editableText.toString()
    override fun getEmail() = emailEditText.editableText.toString()

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun finishApplication() {
        ActivityCompat.finishAffinity(this)
    }
}
