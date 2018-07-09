package com.pachatary.presentation.experience.router

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import kotlinx.android.synthetic.main.activity_register.*
import javax.inject.Inject


class ExperienceRouterActivity : AppCompatActivity(), ExperienceRouterView {

    lateinit var progressBar: ProgressBar
    lateinit var retryView: ImageView
    lateinit var experienceShareId: String

    @Inject
    lateinit var presenter: ExperienceRouterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience_router)
        setSupportActionBar(toolbar)

        experienceShareId = intent.data.pathSegments[1]

        progressBar = findViewById(R.id.experience_router_progressbar)
        retryView = findViewById(R.id.experience_router_retry)
        retryView.setOnClickListener { presenter.onRetryClick() }

        PachataryApplication.injector.inject(this)
        presenter.setViewAndExperienceShareId(this, experienceShareId)
        lifecycle.addObserver(presenter)
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun showErrorMessage() {
        Toast.makeText(this, "Some error occurred. Connect to internet and retry",
                       Toast.LENGTH_LONG).show()
    }

    override fun showRetryView() {
        retryView.visibility = View.VISIBLE
    }

    override fun hideRetryView() {
        retryView.visibility = View.INVISIBLE
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(this, experienceId))
    }
}
