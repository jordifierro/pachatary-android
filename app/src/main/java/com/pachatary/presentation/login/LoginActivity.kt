package com.pachatary.presentation.login

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


class LoginActivity : AppCompatActivity(), LoginView {

    lateinit var progressBar: ProgressBar
    var loginToken: String? = null

    @Inject
    lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        loginToken = intent.data.getQueryParameter("token")

        progressBar = findViewById(R.id.login_progressbar)

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

    override fun loginToken() = loginToken!!

    override fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun showSuccessMessage() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show()
    }

    override fun showErrorMessage() {
        Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show()
    }
}
