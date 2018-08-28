package com.pachatary.presentation.router

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.profile.ProfileActivity
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import javax.inject.Inject


class RouterActivity : AppCompatActivity(), RouterView {

    private lateinit var progressBar: ProgressBar
    lateinit var rootView: RelativeLayout
    lateinit var experienceShareId: String
    lateinit var username: String

    @Inject
    lateinit var experienceRouterPresenter: ExperienceRouterPresenter
    @Inject
    lateinit var profileRouterPresenter: ProfileRouterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        progressBar = findViewById(R.id.progressbar)
        rootView = findViewById(R.id.root)

        PachataryApplication.injector.inject(this)
        val path = intent.data.pathSegments[0]
        if (path == "e" || path == "experiences") {
            experienceShareId = intent.data.pathSegments[1]
            experienceRouterPresenter.setViewAndExperienceShareId(this, experienceShareId)
            lifecycle.addObserver(experienceRouterPresenter)
        }
        else if (path == "p" || path == "profiles") {
            username = intent.data.pathSegments[1]
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

    override fun showRetryView() {
        SnackbarUtils.showRetry(rootView, this) { retryClick() }
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(this, experienceId))
    }

    override fun navigateToProfile(username: String) {
        startActivity(ProfileActivity.newIntent(this, username))
    }

    private fun retryClick() {
        val path = intent.data.pathSegments[0]
        if (path == "e" || path == "experiences") experienceRouterPresenter.onRetryClick()
        else profileRouterPresenter.onRetryClick()
    }
}
