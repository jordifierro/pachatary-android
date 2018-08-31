package com.pachatary.presentation.register

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


class ConfirmEmailActivity : AppCompatActivity(), ConfirmEmailView {

    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: RelativeLayout
    private var confirmationToken: String? = null

    @Inject
    lateinit var presenter: ConfirmEmailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        confirmationToken = intent.data.getQueryParameter("token")

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

    override fun confirmationToken() = confirmationToken!!

    override fun navigateToMain() {
        Handler().postDelayed({ startActivity(MainActivity.newIntent(this)) }, 2000)
    }

    override fun showSuccessMessage() {
        SnackbarUtils.showSuccess(rootView, this, getString(R.string.activity_confirm_email_success_message))
    }

    override fun showRetry() {
        SnackbarUtils.showRetry(rootView, this, { presenter.onRetryClick() })
    }

    override fun showInvalidTokenMessage() {
        SnackbarUtils.showError(rootView, this, getString(R.string.activity_confirm_email_error_message))
    }

    override fun navigateToRegisterWithDelay() {
        Handler().postDelayed({ startActivity(RegisterActivity.newIntent(this)) }, 2000)
    }
}
