package com.pachatary.presentation.experience.show

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
import android.widget.ProgressBar
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.view.SquareViewHolder
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
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
    var experiences: List<Experience> = listOf()

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
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ProfileAdapter(layoutInflater,
                { id -> presenter.onExperienceClick(id) }, { presenter.lastExperienceShown() })
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
        (recyclerView.adapter as ProfileAdapter).inProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as ProfileAdapter).inProgress = false
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

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(this, experienceId))
    }

    class ProfileAdapter(private val inflater: LayoutInflater,
                         val onClick: (String) -> Unit,
                         private val onLastItemShown: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var inProgress = true
        var paginationInProgress = false
        var experiences: List<Experience> = listOf()
        var profileInProgress = true
        var profile: Profile? = null

        val LOADER_TYPE = 0
        val PROFILE_TYPE = 1
        val EXPERIENCE_TYPE = 2


        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                if (profileInProgress) return LOADER_TYPE
                else return PROFILE_TYPE
            }
            else if (position == experiences.size + 1) return LOADER_TYPE
            return EXPERIENCE_TYPE
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (position == experiences.size + 1) {}
            else if (position == 0) {
                if (!profileInProgress) {
                    val profileViewHolder = holder as ProfileViewHolder
                    profileViewHolder.bind(profile!!)
                }
            }
            else {
                val endHasBeenReached = position == experiences.size
                if (experiences.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
                val experienceViewHolder = holder as SquareViewHolder
                experienceViewHolder.bind(experiences[position-1])
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                LOADER_TYPE ->
                    return LoaderViewHolder(inflater.inflate(R.layout.item_loader, parent, false))
                PROFILE_TYPE ->
                    return ProfileViewHolder(inflater.inflate(R.layout.item_profile, parent, false))
                else ->
                    return SquareViewHolder(
                            inflater.inflate(R.layout.item_square_experiences_list, parent, false),
                            onClick)
            }
        }

        override fun getItemCount(): Int {
            if (inProgress || paginationInProgress) return experiences.size + 2
            return experiences.size + 1
        }

        class ProfileViewHolder(view: View)
            : RecyclerView.ViewHolder(view), View.OnClickListener {

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

            override fun onClick(view: View?) {}
        }

        class LoaderViewHolder(view: View)
            : RecyclerView.ViewHolder(view), View.OnClickListener {

            override fun onClick(view: View?) {}
        }
    }
}
