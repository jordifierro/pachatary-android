package com.pachatary.presentation.experience.show

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.EditTextWithBackListener
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.pachatary.presentation.experience.edition.CreateExperienceActivity
import com.pachatary.presentation.experience.show.view.SquareViewHolder
import com.pachatary.presentation.main.SettingsActivity
import com.pachatary.presentation.register.RegisterActivity
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject


class MyExperiencesFragment : Fragment(), MyExperiencesView {

    companion object {
        private const val SELECT_IMAGE = 1

        fun newInstance() = MyExperiencesFragment()
    }

    @Inject
    lateinit var presenter: MyExperiencesPresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var rootView: CoordinatorLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var createExperienceButton: FloatingActionButton
    private var experiences = listOf<Experience>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_experiences, container, false)

        ToolbarUtils.setUp(view, activity as AppCompatActivity, "", false)

        rootView = view.findViewById(R.id.root)
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        createExperienceButton = view.findViewById(R.id.create_new_experience_button)
        createExperienceButton.setOnClickListener { presenter.onCreateExperienceClick() }
        recyclerView.adapter = MyProfileAdapter(layoutInflater, pictureDeviceCompat,
                onExperienceClick = { presenter.onExperienceClick(it) },
                onLastItemShown = { presenter.lastExperienceShown() },
                onProfilePictureClick = { presenter.onProfilePictureClick() },
                onBioEdited = { presenter.onBioEdited(it) })
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (position == 0) return 2
                        if (position == experiences.size + 1) return 2
                        return 1
                    }
                }

        presenter.create()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.myexperiences, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.settings) presenter.onSettingsClick()
        if (item.itemId == R.id.share) presenter.onShareClick()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun showExperiencesLoader() {
        (recyclerView.adapter as MyProfileAdapter).experiencesInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideExperiencesLoader() {
        (recyclerView.adapter as MyProfileAdapter).experiencesInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperiencesRetry() {
        SnackbarUtils.showRetry(rootView, activity as AppCompatActivity)
            { presenter.onRetryClick() }
    }

    override fun showPaginationLoader() {
        (recyclerView.adapter as MyProfileAdapter).paginationInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as MyProfileAdapter).paginationInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experiences: List<Experience>) {
        this.experiences = experiences
        (recyclerView.adapter as MyProfileAdapter).experiences = experiences
        (recyclerView.adapter as MyProfileAdapter).noExperiences = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showNoExperiencesInfo() {
        (recyclerView.adapter as MyProfileAdapter).noExperiences = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfile(profile: Profile) {
        (recyclerView.adapter as MyProfileAdapter).profile = profile
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideProfileLoader() {
        (recyclerView.adapter as MyProfileAdapter).profileInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileLoader() {
        (recyclerView.adapter as MyProfileAdapter).profileInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileRetry() {
        SnackbarUtils.showRetry(rootView, activity as AppCompatActivity)
            { presenter.onRetryClick() }
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(
                ExperienceScenesActivity.newIntent(activity!!.applicationContext, experienceId, true))
    }

    override fun navigateToCreateExperience() {
        startActivity(CreateExperienceActivity.newIntent(context = activity!!.applicationContext))
    }

    override fun navigateToRegister() {
        startActivity(RegisterActivity.newIntent(context = activity!!.applicationContext))
    }

    override fun navigateToPickAndCropImage() {
        startActivityForResult(PickAndCropImageActivity.newIntent(activity!!), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageSelected(PickAndCropImageActivity.getImageUriFrom(data!!))
    }

    override fun navigateToSettings() {
        startActivity(SettingsActivity.newIntent(activity!!.applicationContext))
    }

    override fun showShareDialog(username: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT,
                   "http://" + getString(R.string.https_deeplink_host) + "/p/" + username)
        startActivity(Intent.createChooser(i, "Share URL"))
    }

    override fun showNotEnoughInfoToShareDialog() {
        SnackbarUtils.showError(rootView, activity as AppCompatActivity,
                                getString(R.string.fragment_myexperiences_title))
    }

    class MyProfileAdapter(private val inflater: LayoutInflater,
                           val pictureDeviceCompat: PictureDeviceCompat,
                           val onExperienceClick: (String) -> Unit,
                           private val onLastItemShown: () -> Unit,
                           private val onProfilePictureClick: () -> Unit,
                           private val onBioEdited: (String) -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val LOADER_TYPE = 0
            private const val PROFILE_TYPE = 1
            private const val EXPERIENCE_TYPE = 2
            private const val NO_EXPERIENCES_INFO = 3
        }

        var experiencesInProgress = false
        var paginationInProgress = false
        var noExperiences = false
        var experiences: List<Experience> = listOf()

        var profileInProgress = false
        var profile: Profile? = null

        override fun getItemCount(): Int {
            return if (!experiencesInProgress && !profileInProgress
                    && experiences.isEmpty() && profile == null && !noExperiences) 0
            else if (profileInProgress && experiencesInProgress) 1
            else if (profileInProgress) experiences.size + 1
            else if (experiencesInProgress || noExperiences) 2
            else if (paginationInProgress) experiences.size + 2
            else experiences.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                if (profileInProgress) LOADER_TYPE
                else PROFILE_TYPE
            } else if (position == 1) {
                when {
                    experiencesInProgress -> LOADER_TYPE
                    noExperiences -> NO_EXPERIENCES_INFO
                    else -> EXPERIENCE_TYPE
                }
            } else if (position == experiences.size + 1) LOADER_TYPE
            else EXPERIENCE_TYPE
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                LOADER_TYPE -> object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
                NO_EXPERIENCES_INFO -> object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_no_mine_experiences, parent, false)) {}
                PROFILE_TYPE ->
                    ProfileViewHolder(
                            inflater.inflate(R.layout.item_editable_profile, parent, false),
                            pictureDeviceCompat, onProfilePictureClick, onBioEdited)
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

        class ProfileViewHolder(view: View, private val pictureDeviceCompat: PictureDeviceCompat,
                                private val onProfilePictureClick: () -> Unit,
                                private val onBioEdited: (String) -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

            private val usernameView: TextView = view.findViewById(R.id.username)
            private val bioView: EditTextWithBackListener = view.findViewById(R.id.bio)
            private val pictureView: ImageView = view.findViewById(R.id.picture)
            private val editPictureView: ImageView = view.findViewById(R.id.picture_edit_image)

            init {
                bioView.imeOptions = EditorInfo.IME_ACTION_DONE
                bioView.setRawInputType(InputType.TYPE_CLASS_TEXT)
                bioView.setOnKeyListener { _, keyCode, event ->
                    if ((event.action == KeyEvent.ACTION_DOWN)
                            && (keyCode == KeyEvent.KEYCODE_ENTER)
                        || event.action == KeyEvent.KEYCODE_BACK)
                        onBioEdited(bioView.text.toString())
                    false
                }
                bioView.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                        onBioEdited(bioView.text.toString())
                    false
                }
                bioView.listener = { onBioEdited(bioView.text.toString()) }
            }

            fun bind(profile: Profile) {
                usernameView.text = profile.username
                bioView.setText(profile.bio)
                usernameView.requestFocus()
                bioView.clearFocus()
                if (profile.picture != null) editPictureView.visibility = View.GONE
                Picasso.with(pictureView.context)
                        .load(pictureDeviceCompat.convert(profile.picture)?.halfScreenSizeUrl)
                        .transform(CropCircleTransformation())
                        .into(pictureView)
                pictureView.setOnClickListener { onProfilePictureClick.invoke() }
            }
        }
    }
}
