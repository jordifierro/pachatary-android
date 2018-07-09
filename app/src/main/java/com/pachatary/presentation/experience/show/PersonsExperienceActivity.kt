package com.pachatary.presentation.experience.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.view.ExtendedListAdapter
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import kotlinx.android.synthetic.main.activity_persons_experiences.*
import javax.inject.Inject

class PersonsExperienceActivity : AppCompatActivity(), PersonsExperiencesView {

    companion object {
        val USERNAME = "username"

        fun newIntent(context: Context, username: String): Intent {
            val intent = Intent(context, PersonsExperienceActivity::class.java)
            intent.putExtra(USERNAME, username)
            return intent
        }
    }

    @Inject
    lateinit var presenter: PersonsExperiencesPresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var retryIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_persons_experiences)
        setSupportActionBar(toolbar)
        val username = intent.getStringExtra(USERNAME)
        supportActionBar!!.title = username

        progressBar = findViewById(R.id.experiences_progressbar)
        retryIcon = findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.adapter = ExtendedListAdapter(layoutInflater, listOf(), false,
                { id -> presenter.onExperienceClick(id) }, {}, { presenter.lastExperienceShown() })

        PachataryApplication.injector.inject(this)
        presenter.setViewAndUsername(this, username)
        lifecycle.addObserver(presenter)
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    override fun showRetry() {
        retryIcon.visibility = View.VISIBLE
    }

    override fun hideRetry() {
        retryIcon.visibility = View.GONE
    }

    override fun showPaginationLoader() {
        (recyclerView.adapter as ExtendedListAdapter).inProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as ExtendedListAdapter).inProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        (recyclerView.adapter as ExtendedListAdapter).experienceList = experienceList
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(this, experienceId))
    }
}
