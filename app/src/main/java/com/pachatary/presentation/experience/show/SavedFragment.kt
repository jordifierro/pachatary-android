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
import com.squareup.picasso.Picasso
import javax.inject.Inject

class SavedFragment : Fragment(), SavedView {

    companion object {
        fun newInstance(): SavedFragment {
            val experiencesMineFragment = SavedFragment()
            return experiencesMineFragment
        }
    }

    @Inject
    lateinit var presenter: SavedPresenter

    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var retryIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_saved, container, false)

        progressBar = view.findViewById(R.id.experiences_progressbar)
        retryIcon = view.findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)

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
        recyclerView.adapter = ExperiencesListAdapter(layoutInflater, experienceList,
                { id -> presenter.onExperienceClick(id) })
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(activity, experienceId))
    }

    class ExperiencesListAdapter(val inflater: LayoutInflater, val experienceList: List<Experience>,
                                 val onClick: (String) -> Unit) : RecyclerView.Adapter<ExperienceViewHolder>() {

        override fun onBindViewHolder(holder: ExperienceViewHolder?, position: Int) {
            holder?.bind(experienceList[position])
        }

        override fun getItemCount(): Int = experienceList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ExperienceViewHolder {
            return ExperienceViewHolder(inflater.inflate(R.layout.item_experiences_list,
                                        parent, false), onClick)
        }
    }

    class ExperienceViewHolder(view: View, val onClick: (String) -> Unit)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView
        private val pictureView: ImageView
        lateinit var experienceId: String

        init {
            titleView = view.findViewById(R.id.experience_title)
            pictureView = view.findViewById(R.id.experience_picture)
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
