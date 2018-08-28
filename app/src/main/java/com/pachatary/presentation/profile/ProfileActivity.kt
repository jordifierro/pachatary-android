package com.pachatary.presentation.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.pachatary.presentation.experience.show.view.SquareViewHolder
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject

class ProfileActivity : AppCompatActivity(), ProfileView {

    companion object {
        const val USERNAME = "username"

        fun newIntent(context: Context, username: String): Intent {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(USERNAME, username)
            return intent
        }
    }

    @Inject
    lateinit var presenter: ProfilePresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var recyclerView: RecyclerView
    private lateinit var rootView: CoordinatorLayout
    var experiences: List<Experience> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val username = intent.getStringExtra(USERNAME)

        PachataryApplication.injector.inject(this)

        rootView = findViewById(R.id.root)
        ToolbarUtils.setUp(this, "", true)

        recyclerView = findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ProfileAdapter(layoutInflater, pictureDeviceCompat,
                { id -> presenter.onExperienceClick(id) }, { presenter.lastExperienceShown() })
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (position == 0) return 2
                        if (position == experiences.size + 1) return 2
                        return 1
                    }
                }

        presenter.setViewAndUsername(this, username)
        lifecycle.addObserver(presenter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.share -> {
                presenter.onShareClick()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun showRetry() {
        SnackbarUtils.showRetry(rootView, this) { presenter.onRetryClick() }
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

    override fun showShareDialog(username: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT,
                "http://" + getString(R.string.https_deeplink_host) + "/p/" + username)
        startActivity(Intent.createChooser(i, "Share URL"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class ProfileAdapter(private val inflater: LayoutInflater,
                         val pictureDeviceCompat: PictureDeviceCompat,
                         val onExperienceClick: (String) -> Unit,
                         private val onLastItemShown: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val LOADER_TYPE = 0
            const val PROFILE_TYPE = 2
            const val EXPERIENCE_TYPE = 3
        }

        var experiencesInProgress = true
        var paginationInProgress = false
        var experiences: List<Experience> = listOf()

        var profileInProgress = true
        var profile: Profile? = null

        override fun getItemCount(): Int {
            return if (profileInProgress && experiencesInProgress) 1
            else if (profileInProgress) experiences.size + 1
            else if (experiencesInProgress) 2
            else if (paginationInProgress) experiences.size + 2
            else experiences.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                if (profileInProgress) LOADER_TYPE
                else PROFILE_TYPE
            } else if (position == 1) {
                if (experiencesInProgress) LOADER_TYPE
                else EXPERIENCE_TYPE
            } else if (position == experiences.size + 1) LOADER_TYPE
            else EXPERIENCE_TYPE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                LOADER_TYPE -> object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
                PROFILE_TYPE ->
                    ProfileViewHolder(inflater.inflate(R.layout.item_profile, parent, false),
                            pictureDeviceCompat)
                else ->
                    SquareViewHolder(
                            inflater.inflate(R.layout.item_square_experiences_list, parent, false),
                            onExperienceClick, pictureDeviceCompat)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                PROFILE_TYPE -> {
                    val profileViewHolder = holder as ProfileViewHolder
                    if (profile != null) profileViewHolder.bind(profile!!)
                }
                EXPERIENCE_TYPE -> {
                    val experienceViewHolder = holder as SquareViewHolder
                    experienceViewHolder.bind(experiences[position-1])
                }
                else -> {}
            }

            if (!experiencesInProgress) {
                val endHasBeenReached = position == experiences.size
                if (experiences.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            }
        }

        class ProfileViewHolder(view: View, private val pictureDeviceCompat: PictureDeviceCompat)
                                                                   : RecyclerView.ViewHolder(view) {

            private val usernameView: TextView = view.findViewById(R.id.username)
            private val bioView: TextView = view.findViewById(R.id.bio)
            private val pictureView: ImageView = view.findViewById(R.id.picture)

            fun bind(profile: Profile) {
                usernameView.text = profile.username
                bioView.text = profile.bio
                Picasso.with(pictureView.context)
                        .load(pictureDeviceCompat.convert(profile.picture)?.halfScreenSizeUrl)
                        .transform(CropCircleTransformation())
                        .into(pictureView)
            }
        }
    }
}
