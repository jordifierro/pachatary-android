package com.pachatary.presentation.experience.show

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.scene.show.ExperienceMapActivity
import com.squareup.picasso.Picasso
import javax.inject.Inject

class ExploreFragment : Fragment(), ExploreView {

    companion object {
        fun newInstance() = ExploreFragment()
    }

    @Inject
    lateinit var presenter: ExplorePresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var retryIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        progressBar = view.findViewById(R.id.experiences_progressbar)
        retryIcon = view.findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 1)
        recyclerView.adapter = ExperiencesListAdapter(layoutInflater, listOf(),
                { id -> presenter.onExperienceClick(id) })

        presenter.create()
        return view
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
        (recyclerView.adapter as ExperiencesListAdapter).experienceList = experienceList
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(activity!!.applicationContext, experienceId))
    }

    class ExperiencesListAdapter(private val inflater: LayoutInflater,
                                 var experienceList: List<Experience>,
                                 val onClick: (String) -> Unit)
                                                    : RecyclerView.Adapter<ExperienceViewHolder>() {

        override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
            holder.bind(experienceList[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
            return ExperienceViewHolder(inflater.inflate(R.layout.item_full_experiences_list,
                    parent, false), onClick)
        }

        override fun getItemCount(): Int = experienceList.size
    }

    class ExperienceViewHolder(view: View, val onClick: (String) -> Unit)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView = view.findViewById(R.id.experience_title)
        private val authorView: TextView = view.findViewById(R.id.experience_author)
        private val descriptionView: TextView = view.findViewById(R.id.experience_description)
        private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
        lateinit var experienceId: String

        init {
            view.setOnClickListener(this)
        }

        fun bind(experience: Experience) {
            this.experienceId = experience.id
            titleView.text = experience.title
            authorView.text = "by " + experience.author_username
            descriptionView.text = experience.description
            Picasso.with(pictureView.context)
                    .load(experience.picture?.mediumUrl)
                    .into(pictureView)
        }

        override fun onClick(view: View?) = this.onClick(this.experienceId)
    }
}
