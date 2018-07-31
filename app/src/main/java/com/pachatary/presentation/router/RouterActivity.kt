package com.pachatary.presentation.router

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.profile.ProfileActivity
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import kotlinx.android.synthetic.main.activity_router.*
import javax.inject.Inject


class RouterActivity : AppCompatActivity(), RouterView {

    lateinit var progressBar: ProgressBar
    lateinit var retryView: ImageView
    lateinit var experienceShareId: String
    lateinit var username: String

    @Inject
    lateinit var experienceRouterPresenter: ExperienceRouterPresenter
    @Inject
    lateinit var profileRouterPresenter: ProfileRouterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_router)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.router_progressbar)
        retryView = findViewById(R.id.router_retry)

        PachataryApplication.injector.inject(this)
        val path = intent.data.pathSegments[0]
        if (path == "e") {
            experienceShareId = intent.data.pathSegments[1]
            retryView.setOnClickListener { experienceRouterPresenter.onRetryClick() }
            experienceRouterPresenter.setViewAndExperienceShareId(this, experienceShareId)
            lifecycle.addObserver(experienceRouterPresenter)
        }
        else if (path == "p") {
            username = intent.data.pathSegments[1]
            retryView.setOnClickListener { profileRouterPresenter.onRetryClick() }
            profileRouterPresenter.setViewAndUsername(this, username)
            lifecycle.addObserver(profileRouterPresenter)
        }
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

    override fun navigateToProfile(username: String) {
        startActivity(ProfileActivity.newIntent(this, username))
    }
}
