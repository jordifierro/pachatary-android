package com.pachatary.presentation.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.view.SquareViewHolder
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject

class ProfileActivity : AppCompatActivity(), ProfileView {

    companion object {
        val USERNAME = "username"

        fun newIntent(context: Context, username: String): Intent {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(USERNAME, username)
            return intent
        }
    }

    @Inject
    lateinit var presenter: ProfilePresenter

    private lateinit var recyclerView: RecyclerView
    var experiences: List<Experience> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val username = intent.getStringExtra(USERNAME)

        recyclerView = findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ProfileAdapter(layoutInflater,
                { id -> presenter.onExperienceClick(id) }, { presenter.lastExperienceShown() },
                { presenter.onRetryClick() })
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (position == 0) return 2
                        if (position == experiences.size + 1) return 2
                        return 1
                    }
                }

        PachataryApplication.injector.inject(this)
        presenter.setViewAndUsername(this, username)
        lifecycle.addObserver(presenter)
    }

    override fun showExperiencesRetry() {
        (recyclerView.adapter as ProfileAdapter).experiencesError = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideExperiencesRetry() {
        (recyclerView.adapter as ProfileAdapter).experiencesError = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperiencesLoader() {
        (recyclerView.adapter as ProfileAdapter).experiencesInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideExperiencesLoader() {
        (recyclerView.adapter as ProfileAdapter).experiencesInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileLoader() {
        (recyclerView.adapter as ProfileAdapter).profileInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideProfileLoader() {
        (recyclerView.adapter as ProfileAdapter).profileInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileRetry() {
        (recyclerView.adapter as ProfileAdapter).profileError = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideProfileRetry() {
        (recyclerView.adapter as ProfileAdapter).profileError = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showPaginationLoader() {
        (recyclerView.adapter as ProfileAdapter).paginationInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as ProfileAdapter).paginationInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        this.experiences = experienceList
        (recyclerView.adapter as ProfileAdapter).experiences = experienceList
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfile(profile: Profile) {
        (recyclerView.adapter as ProfileAdapter).profileInProgress = false
        (recyclerView.adapter as ProfileAdapter).profile = profile
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperienceWithFinishOnProfileClick(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(this, experienceId,
                finishOnProfileClick = true))
    }

    class ProfileAdapter(private val inflater: LayoutInflater,
                         val onExperienceClick: (String) -> Unit,
                         private val onLastItemShown: () -> Unit,
                         private val onRetryClick: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var experiencesInProgress = true
        var experiencesError = false
        var paginationInProgress = false
        var experiences: List<Experience> = listOf()

        var profileInProgress = true
        var profileError = false
        var profile: Profile? = null

        val LOADER_TYPE = 0
        val RETRY_TYPE = 1
        val PROFILE_TYPE = 2
        val EXPERIENCE_TYPE = 3

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                if (experiencesError || profileError) return RETRY_TYPE
                else if (profileInProgress) return LOADER_TYPE
                else return PROFILE_TYPE
            }
            else if (position == 1) {
                if (experiencesInProgress) return LOADER_TYPE
                else return EXPERIENCE_TYPE
            }
            else if (position == experiences.size + 1) return LOADER_TYPE
            else return EXPERIENCE_TYPE
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                PROFILE_TYPE -> {
                    val profileViewHolder = holder as ProfileViewHolder
                    profileViewHolder.bind(profile!!)
                }
                EXPERIENCE_TYPE -> {
                    val experienceViewHolder = holder as SquareViewHolder
                    experienceViewHolder.bind(experiences[position-1])
                }
                else -> {}
            }

            if (!experiencesError && !experiencesInProgress) {
                val endHasBeenReached = position == experiences.size
                if (experiences.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                LOADER_TYPE -> return object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
                RETRY_TYPE -> {
                    val view = inflater.inflate(R.layout.item_retry, parent, false)
                    val viewHolder =  object : RecyclerView.ViewHolder(view), View.OnClickListener {
                        override fun onClick(v: View?) { onRetryClick.invoke() } }
                    view.setOnClickListener(viewHolder)
                    return viewHolder
                }
                PROFILE_TYPE ->
                    return ProfileViewHolder(inflater.inflate(R.layout.item_profile, parent, false))
                else ->
                    return SquareViewHolder(
                            inflater.inflate(R.layout.item_square_experiences_list, parent, false),
                            onExperienceClick)
            }
        }

        override fun getItemCount(): Int {
            if (experiencesError || profileError) return 1
            else if (profileInProgress && experiencesInProgress) return 1
            else if (profileInProgress) return experiences.size + 1
            else if (experiencesInProgress) return 2
            else if (paginationInProgress) return experiences.size + 2
            else return experiences.size + 1
        }

        class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val usernameView: TextView = view.findViewById(R.id.username)
            private val bioView: TextView = view.findViewById(R.id.bio)
            private val pictureView: ImageView = view.findViewById(R.id.picture)

            fun bind(profile: Profile) {
                usernameView.text = profile.username
                bioView.text = profile.bio
                Picasso.with(pictureView.context)
                        .load(profile.picture?.smallUrl)
                        .transform(CropCircleTransformation())
                        .into(pictureView)
            }
        }
    }
}
