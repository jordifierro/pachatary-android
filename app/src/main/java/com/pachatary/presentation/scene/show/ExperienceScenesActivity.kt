package com.pachatary.presentation.scene.show

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.mapbox.api.staticmap.v1.MapboxStaticMap
import com.mapbox.api.staticmap.v1.StaticMapCriteria
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation
import com.mapbox.geojson.Point
import com.pachatary.BuildConfig
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.experience.edition.EditExperienceActivity
import com.pachatary.presentation.profile.ProfileActivity
import com.pachatary.presentation.scene.edition.CreateSceneActivity
import com.pachatary.presentation.scene.edition.EditSceneActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject

class ExperienceScenesActivity : AppCompatActivity(), ExperienceScenesView {

    @Inject
    lateinit var presenter: ExperienceScenesPresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var recyclerView: RecyclerView
    private lateinit var rootView: CoordinatorLayout
    private lateinit var addButton: FloatingActionButton
    private lateinit var shareButton: FloatingActionButton
    private lateinit var editButton: FloatingActionButton
    private lateinit var saveButton: FloatingActionButton
    private lateinit var unsaveButton: FloatingActionButton

    private var experience: Experience? = null

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private const val MAP_INTENT = 1
        private const val EXPERIENCE_ID = "experienceId"
        private const val SHOW_EDITABLE_IF_ITS_MINE = "show_editable_if_its_mine"
        private const val FINISH_ON_PROFILE_CLICK = "finish_on_profile_click"

        fun newIntent(context: Context, experienceId: String,
                      showEditableIfItsMine: Boolean = false,
                      finishOnProfileClick: Boolean = false): Intent {
            val intent = Intent(context, ExperienceScenesActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SHOW_EDITABLE_IF_ITS_MINE, showEditableIfItsMine)
            intent.putExtra(FINISH_ON_PROFILE_CLICK, finishOnProfileClick)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience_scenes)

        findViewById<AppBarLayout>(R.id.appbar)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))

        recyclerView = findViewById(R.id.scenes_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        rootView = findViewById(R.id.root)
        addButton = findViewById(R.id.add_button)
        addButton.setOnClickListener { presenter.onAddSceneButtonClick() }
        shareButton = findViewById(R.id.share_button)
        shareButton.setOnClickListener { presenter.onShareClick() }
        editButton = findViewById(R.id.edit_button)
        editButton.setOnClickListener { presenter.onEditExperienceClick() }
        saveButton = findViewById(R.id.save_button)
        saveButton.setOnClickListener { presenter.onExperienceSave(true) }
        unsaveButton = findViewById(R.id.unsave_button)
        unsaveButton.setOnClickListener { presenter.onExperienceSave(false) }
        findViewById<ImageView>(R.id.back_button).setOnClickListener { super.onBackPressed() }

        PachataryApplication.injector.inject(this)
        presenter.setView(view = this, experienceId = intent.getStringExtra(EXPERIENCE_ID),
              finishOnProfileClick = intent.getBooleanExtra(FINISH_ON_PROFILE_CLICK, false))
        registry.addObserver(presenter)

        recyclerView.adapter =
                ExperienceScenesAdapter(layoutInflater,
                                        intent.getBooleanExtra(SHOW_EDITABLE_IF_ITS_MINE, false),
                                        pictureDeviceCompat, presenter,
                                        findViewById(R.id.appbar))
    }

    override fun navigateToEditScene(sceneId: String, experienceId: String) {
        startActivity(EditSceneActivity.newIntent(this, experienceId, sceneId))
    }

    override fun navigateToEditExperience(experienceId: String) {
        startActivity(EditExperienceActivity.newIntent(this, experienceId))
    }

    override fun showExperience(experience: Experience) {
        this.experience = experience

        (recyclerView.adapter as ExperienceScenesAdapter).experience = experience
        (recyclerView.adapter as ExperienceScenesAdapter).isExperienceInProgress = false
        recyclerView.adapter.notifyDataSetChanged()

        setAllButtonsGone()
        if (intent.getBooleanExtra(SHOW_EDITABLE_IF_ITS_MINE, false) && experience.isMine) {
            addButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE
            editButton.visibility = View.VISIBLE
        }
        else if (experience.isMine) {
            shareButton.visibility = View.VISIBLE
        } else {
            shareButton.visibility = View.VISIBLE
            if (experience.isSaved) unsaveButton.visibility = View.VISIBLE
            else saveButton.visibility = View.VISIBLE
        }
    }

    override fun showScenes(scenes: List<Scene>) {
        (recyclerView.adapter as ExperienceScenesAdapter).scenes = scenes
        (recyclerView.adapter as ExperienceScenesAdapter).areScenesInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showLoadingExperience() {
        (recyclerView.adapter as ExperienceScenesAdapter).isExperienceInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showLoadingScenes() {
        (recyclerView.adapter as ExperienceScenesAdapter).areScenesInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showRetry() {
        (recyclerView.adapter as ExperienceScenesAdapter).areScenesInProgress = false
        (recyclerView.adapter as ExperienceScenesAdapter).isExperienceInProgress = false
        recyclerView.adapter.notifyDataSetChanged()

        SnackbarUtils.showRetry(rootView, this) { presenter.onRetryClick() }
    }

    override fun navigateToExperienceMap(experienceId: String, showSceneWithId: String?) {
        startActivityForResult(ExperienceMapActivity.newIntent(this, experienceId, showSceneWithId),
                               MAP_INTENT)
    }

    override fun navigateToCreateScene(experienceId: String) {
        startActivity(CreateSceneActivity.newIntent(this, experienceId))
    }

    override fun showUnsaveDialog() {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        else AlertDialog.Builder(this)
        builder.setTitle(R.string.activity_experience_scenes_unsave_dialog_title)
                .setMessage(R.string.activity_experience_scenes_unsave_dialog_text)
                .setPositiveButton(android.R.string.yes) { _, _ -> presenter.onConfirmUnsaveExperience() }
                .setNegativeButton(android.R.string.no) { _, _ -> presenter.onCancelUnsaveExperience() }
                .show()
    }

    override fun showSavedMessage() {
        val message = this.resources.getString(R.string.activity_experience_scenes_saved_message)
        SnackbarUtils.showSuccess(rootView, this, message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MAP_INTENT && resultCode == Activity.RESULT_OK) {
            val selectedSceneId = data!!.getStringExtra(ExperienceMapActivity.SCENE_ID)
            presenter.onSceneSelectedOnMap(selectedSceneId)
        }
    }

    override fun scrollToScene(sceneId: String) {
        (recyclerView.adapter as ExperienceScenesAdapter).scrollToScene(sceneId)
    }

    override fun navigateToProfile(username: String) {
        startActivity(ProfileActivity.newIntent(this, username))
    }

    override fun showError() {
        SnackbarUtils.showError(rootView, this)
    }

    override fun showShareDialog(shareUrl: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, shareUrl)
        startActivity(Intent.createChooser(i, "Share URL"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperienceScenesAdapter(private val inflater: LayoutInflater,
                                  private val showEditableIfItsMine: Boolean,
                                  val pictureDeviceCompat: PictureDeviceCompat,
                                  val presenter: ExperienceScenesPresenter,
                                  val appBarLayout: AppBarLayout)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val LOADER_TYPE = 0
            private const val EXPERIENCE_TYPE = 1
            private const val SCENE_TYPE = 2
        }

        lateinit var recyclerView: RecyclerView
        var isExperienceInProgress = true
        var areScenesInProgress = true
        var experience: Experience? = null
        var scenes: List<Scene> = listOf()

        override fun getItemCount(): Int {
            return if (isExperienceInProgress && areScenesInProgress) 1
            else if (areScenesInProgress) 2
            else scenes.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                if (isExperienceInProgress) LOADER_TYPE
                else EXPERIENCE_TYPE
            } else {
                if (areScenesInProgress) LOADER_TYPE
                else SCENE_TYPE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                EXPERIENCE_TYPE -> {
                    return ExperienceViewHolder(inflater.inflate(R.layout.item_experience, parent, false),
                            pictureDeviceCompat,
                            { presenter.onMapButtonClick() },
                            { presenter.onProfileClick(it) })
                }
                SCENE_TYPE -> {
                    var isEditable = false
                    if (showEditableIfItsMine && experience != null && experience!!.isMine)
                        isEditable = true

                    return SceneViewHolder(inflater.inflate(R.layout.item_scene, parent, false),
                            isEditable , pictureDeviceCompat,
                            { presenter.onEditSceneClick(it) },
                            { presenter.onLocateSceneClick(it) })
                }
                else -> {
                    return object : RecyclerView.ViewHolder(
                            inflater.inflate(R.layout.item_loader, parent, false)) {}
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                EXPERIENCE_TYPE -> {
                    val experienceViewHolder = holder as ExperienceViewHolder
                    experienceViewHolder.bind(experience!!, scenes)
                }
                SCENE_TYPE -> {
                    val sceneViewHolder = holder as SceneViewHolder
                    sceneViewHolder.bind(scenes[position-1])
                }
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
        }

        fun scrollToScene(sceneId: String) {
            if (scenes.count { it.id == sceneId } > 0) {
                val selectedPosition = scenes.indexOfFirst { it.id == sceneId }
                val offset = recyclerView.resources.getDimension(R.dimen.toolbar_height) *
                             recyclerView.resources.displayMetrics.density
                val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                    override fun getVerticalSnapPreference() = LinearSmoothScroller.SNAP_TO_START
                    override fun calculateDyToMakeVisible(view: View?, snapPreference: Int) =
                        super.calculateDyToMakeVisible(view, snapPreference) + (offset / 2).toInt()
                }
                smoothScroller.targetPosition = selectedPosition + 1
                appBarLayout.setExpanded(false)
                Handler().postDelayed({
                    try {
                        this.recyclerView.layoutManager.startSmoothScroll(smoothScroller)
                    } catch (e: Exception) {
                    }
                }, 100)
            }
        }
    }

    class ExperienceViewHolder(view: View,
                               private val pictureDeviceCompat: PictureDeviceCompat,
                               private val onMapButtonClick: () -> Unit,
                               private val onProfileClick: (String) -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

        private val titleView: TextView = view.findViewById(R.id.experience_title)
        private val descriptionView: TextView = view.findViewById(R.id.experience_description)
        private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
        private val savesCountView: TextView = view.findViewById(R.id.experience_saves_count)
        private val authorUsernameView: TextView = view.findViewById(R.id.experience_author_username)
        private val authorPictureView: ImageView = view.findViewById(R.id.experience_author_picture)
        private val mapView: ImageView = view.findViewById(R.id.experience_map)
        private val starIcon: ImageView = view.findViewById(R.id.experience_star)
        private val showMoreView: TextView = view.findViewById(R.id.experience_description_show_more)
        lateinit var experienceId: String

        fun bind(experience: Experience, scenes: List<Scene>) {
            this.experienceId = experience.id
            titleView.text = experience.title

            authorPictureView.setOnClickListener {
                onProfileClick(experience.authorProfile.username) }
            authorUsernameView.setOnClickListener {
                onProfileClick(experience.authorProfile.username) }
            savesCountView.text = experience.savesCount.toString()
            authorUsernameView.text = experience.authorProfile.username
            Picasso.with(pictureView.context)
                .load(pictureDeviceCompat.convert(experience.picture)?.fullScreenSizeUrl)
                .into(pictureView)
            Picasso.with(authorPictureView.context)
                .load(pictureDeviceCompat.convert(experience.authorProfile.picture)?.iconSizeUrl)
                .transform(CropCircleTransformation())
                .into(authorPictureView)

            mapView.setOnClickListener { onMapButtonClick() }
            Picasso.with(mapView.context)
                    .load(mapUrl(scenes))
                    .into(mapView)
            if (experience.isSaved) starIcon.setColorFilter(
                    ContextCompat.getColor(starIcon.context, R.color.colorPrimary))
            else starIcon.colorFilter = null

            descriptionView.text = experience.description
            descriptionView.viewTreeObserver.addOnGlobalLayoutListener {
                val layout = descriptionView.layout
                if (layout != null) {
                    if (layout.lineCount > 0) {
                        if (layout.getEllipsisCount(layout.lineCount - 1) > 0) {
                            showMoreView.visibility = View.VISIBLE
                            showMoreView.setOnClickListener {
                                descriptionView.ellipsize = null
                                descriptionView.maxLines = Int.MAX_VALUE
                                descriptionView.text = experience.description
                                showMoreView.visibility = View.INVISIBLE
                            }
                        }
                        else showMoreView.visibility = View.INVISIBLE
                    }
                }
            }
        }

        private fun mapUrl(scenes: List<Scene>): String? {
            if (scenes.isEmpty()) return null
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val markers = mutableListOf<StaticMarkerAnnotation>()
            for (scene in scenes) {
                markers.add(StaticMarkerAnnotation.builder()
                        .lnglat(Point.fromLngLat(scene.longitude, scene.latitude))
                        .iconUrl("https://s3-eu-west-1.amazonaws.com/pachatary/static/circle.png")
                        .build())
            }
            val map = MapboxStaticMap.builder()
                    .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                    .styleId(StaticMapCriteria.LIGHT_STYLE)
                    .width(screenWidth)
                    .height(screenWidth / 2)
                    .staticMarkerAnnotations(markers)
                    .cameraAuto(true)
                    .build()
            return map.url().toString()
        }
    }

    class SceneViewHolder(view: View, private val isEditable: Boolean,
                          private val pictureDeviceCompat: PictureDeviceCompat,
                          private val onEditSceneClick: (String) -> Unit,
                          private val onLocateSceneClick: (String) -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

        private val titleView: TextView = view.findViewById(R.id.title)
        private val descriptionView: TextView = view.findViewById(R.id.description)
        private val showMoreView: TextView = view.findViewById(R.id.description_show_more)
        private val pictureView: ImageView = view.findViewById(R.id.picture)
        private val editButton: FloatingActionButton = view.findViewById(R.id.edit_button)
        private val locateButton: ImageButton = view.findViewById(R.id.locate_button)
        lateinit var sceneId: String

        fun bind(scene: Scene) {
            this.sceneId = scene.id
            titleView.text = scene.title
            editButton.setOnClickListener { onEditSceneClick(this.sceneId) }
            locateButton.setOnClickListener { onLocateSceneClick(this.sceneId) }
            if (isEditable) editButton.visibility = View.VISIBLE
            else editButton.visibility = View.GONE
            Picasso.with(pictureView.context)
                    .load(pictureDeviceCompat.convert(scene.picture)?.fullScreenSizeUrl)
                    .into(pictureView)

            descriptionView.text = scene.description
            descriptionView.viewTreeObserver.addOnGlobalLayoutListener {
                val layout = descriptionView.layout
                if (layout != null) {
                    if (layout.lineCount > 0) {
                        if (layout.getEllipsisCount(layout.lineCount - 1) > 0) {
                            showMoreView.visibility = View.VISIBLE
                            showMoreView.setOnClickListener {
                                descriptionView.ellipsize = null
                                descriptionView.maxLines = Int.MAX_VALUE
                                descriptionView.text = scene.description
                                showMoreView.visibility = View.INVISIBLE
                            }
                        }
                        else showMoreView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun setAllButtonsGone() {
        addButton.visibility = View.GONE
        shareButton.visibility = View.GONE
        editButton.visibility = View.GONE
        saveButton.visibility = View.GONE
        unsaveButton.visibility = View.GONE
    }
}
