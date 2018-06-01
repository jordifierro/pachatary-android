package com.pachatary.presentation.experience.router

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.main.MainActivity
import com.pachatary.presentation.scene.show.ExperienceMapActivity
import kotlinx.android.synthetic.main.activity_register.*
import javax.inject.Inject


class ExperienceRouterActivity : AppCompatActivity(), ExperienceRouterView {

    lateinit var progressBar: ProgressBar
    lateinit var experienceShareId: String

    @Inject
    lateinit var presenter: ExperienceRouterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience_router)
        setSupportActionBar(toolbar)

        experienceShareId = intent.data.pathSegments[1]

        progressBar = findViewById(R.id.experience_router_progressbar)

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

    override fun showSuccessMessage() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show()
    }

    override fun showErrorMessage() {
        Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(this, experienceId))
    }
}
