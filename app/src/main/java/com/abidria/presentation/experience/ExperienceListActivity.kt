package com.abidria.presentation.experience

import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.abidria.R
import com.abidria.data.experience.Experience
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.experience.create.CreateExperienceActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_experiences_list.*
import javax.inject.Inject

class ExperienceListActivity : AppCompatActivity(), ExperienceListView {

    @Inject
    lateinit var presenter: ExperienceListPresenter

    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var retryIcon: ImageView
    lateinit var createExperienceButton: FloatingActionButton

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiences_list)
        setSupportActionBar(toolbar)

        progressBar = findViewById<ProgressBar>(R.id.experiences_progressbar)
        retryIcon = findViewById<ImageView>(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = findViewById<RecyclerView>(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        createExperienceButton = findViewById<FloatingActionButton>(R.id.create_new_experience_button)
        createExperienceButton.setOnClickListener { presenter.onCreateExperienceClick() }

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
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

    override fun showExperienceList(experienceList: List<Experience>) {
        recyclerView.adapter = ExperiencesListAdapter(layoutInflater, experienceList,
                                                      { id -> presenter.onExperienceClick(id) })
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(this, experienceId))
    }

    override fun navigateToCreateExperience() {
        startActivity(CreateExperienceActivity.newIntent(context = this))
    }

    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperiencesListAdapter(val inflater: LayoutInflater, val experienceList: List<Experience>,
                                 val onClick: (String) -> Unit) : RecyclerView.Adapter<ExperienceViewHolder>() {

        override fun onBindViewHolder(holder: ExperienceViewHolder?, position: Int) {
            holder?.bind(experienceList[position])
        }

        override fun getItemCount(): Int = experienceList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ExperienceViewHolder {
            return ExperienceViewHolder(inflater.inflate(R.layout.experiences_list_item, parent, false), onClick)
        }
    }

    class ExperienceViewHolder(view: View, val onClick: (String) -> Unit)
                                                                : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView
        private val pictureView: ImageView
        lateinit var experienceId: String

        init {
            titleView = view.findViewById<TextView>(R.id.experience_title)
            pictureView = view.findViewById<ImageView>(R.id.experience_picture)
            view.setOnClickListener(this)
        }

        fun bind(experience: Experience) {
            this.experienceId = experience.id
            titleView.text = experience.title
            Picasso.with(pictureView.context)
                   .load(experience.picture?.smallUrl)
                   .into(pictureView)
        }

        override fun onClick(view: View?) = this.onClick(this.experienceId)
    }
}
