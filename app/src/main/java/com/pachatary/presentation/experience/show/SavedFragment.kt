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
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.view.SquareListAdapter
import com.pachatary.presentation.scene.show.ExperienceMapActivity
import javax.inject.Inject

class SavedFragment : Fragment(), SavedView {

    companion object {
        fun newInstance() = SavedFragment()
    }

    @Inject
    lateinit var presenter: SavedPresenter

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
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        progressBar = view.findViewById(R.id.experiences_progressbar)
        retryIcon = view.findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        recyclerView.adapter = SquareListAdapter(layoutInflater, listOf(), false,
                { id -> presenter.onExperienceClick(id) }, { presenter.lastExperienceShown() })

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

    override fun showPaginationLoader() {
        (recyclerView.adapter as SquareListAdapter).inProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as SquareListAdapter).inProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        (recyclerView.adapter as SquareListAdapter).experienceList = experienceList
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(activity!!.applicationContext, experienceId))
    }
}
