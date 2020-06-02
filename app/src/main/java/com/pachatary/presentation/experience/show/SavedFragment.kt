package com.pachatary.presentation.experience.show

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.pachatary.presentation.experience.show.view.SquareListAdapter
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import javax.inject.Inject

class SavedFragment : Fragment(), SavedView {

    companion object {
        fun newInstance() = SavedFragment()
    }

    @Inject
    lateinit var presenter: SavedPresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var rootView: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)

        ToolbarUtils.setUp(view, activity as AppCompatActivity,
                           getString(R.string.fragment_saved_experiences_title), false)

        rootView = view.findViewById(R.id.root)
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        recyclerView.adapter = SquareListAdapter(
                layoutInflater, pictureDeviceCompat, listOf(), false, false,
                { id -> presenter.onExperienceClick(id) }, { presenter.lastExperienceShown() })
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val adapter = (recyclerView.adapter as SquareListAdapter)
                        if (adapter.inProgress && position == adapter.experienceList.size) return 2
                        if (adapter.noSavedExperiences) return 2
                        return 1
                    }
                }
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            presenter.onRefresh()
        }

        presenter.create()

        return view
    }

    override fun showLoader() {
        (recyclerView.adapter as SquareListAdapter).inProgress = true
        (recyclerView.adapter as SquareListAdapter).noSavedExperiences = false
        (recyclerView.adapter as SquareListAdapter).notifyDataSetChanged()
    }

    override fun hideLoader() {
        (recyclerView.adapter as SquareListAdapter).inProgress = false
        (recyclerView.adapter as SquareListAdapter).noSavedExperiences = false
        (recyclerView.adapter as SquareListAdapter).notifyDataSetChanged()
    }

    override fun showRetry() {
        SnackbarUtils.showRetry(rootView, activity as AppCompatActivity)
        { presenter.onRetryClick() }
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        (recyclerView.adapter as SquareListAdapter).experienceList = experienceList
        (recyclerView.adapter as SquareListAdapter).noSavedExperiences = false
        (recyclerView.adapter as SquareListAdapter).notifyDataSetChanged()
    }

    override fun showNoSavedExperiencesInfo() {
        (recyclerView.adapter as SquareListAdapter).noSavedExperiences = true
        (recyclerView.adapter as SquareListAdapter).notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(activity!!.applicationContext, experienceId))
    }
}
