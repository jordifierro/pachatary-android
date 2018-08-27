package com.pachatary.presentation.register

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.main.MainActivity
import kotlinx.android.synthetic.main.activity_register.*
import javax.inject.Inject


class ConfirmEmailActivity : AppCompatActivity(), ConfirmEmailView {

    lateinit var progressBar: ProgressBar
    var confirmationToken: String? = null

    @Inject
    lateinit var presenter: ConfirmEmailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_email)

        confirmationToken = intent.data.getQueryParameter("token")

        progressBar = findViewById(R.id.confirm_email_progressbar)

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

    override fun confirmationToken() = confirmationToken!!

    override fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
