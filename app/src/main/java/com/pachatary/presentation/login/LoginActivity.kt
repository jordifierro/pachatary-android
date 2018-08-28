package com.pachatary.presentation.login

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.main.MainActivity
import javax.inject.Inject


class LoginActivity : AppCompatActivity(), LoginView {

    lateinit var progressBar: ProgressBar
    lateinit var rootView: RelativeLayout
    var loginToken: String? = null

    @Inject
    lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loginToken = intent.data.getQueryParameter("token")

        rootView = findViewById(R.id.root)
        progressBar = findViewById(R.id.progressbar)

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

    override fun showSuccessMessage() {
        SnackbarUtils.showSuccess(rootView, this, getString(R.string.login_success))
    }

    override fun showErrorMessage() {
        SnackbarUtils.showError(rootView, this, getString(R.string.login_error))
    }

    override fun showRetry() {
        SnackbarUtils.showRetry(rootView, this, { presenter.retryClick() })
    }

    override fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun navigateToAskLoginEmailWithDelay() {
        Handler().postDelayed({ startActivity(AskLoginEmailActivity.newIntent(this)) }, 2000)
    }

    override fun finishWithDelay() {
        Handler().postDelayed({ finish() }, 2000)
    }
}
