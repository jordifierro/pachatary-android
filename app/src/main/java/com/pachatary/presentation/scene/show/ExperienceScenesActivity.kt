package com.pachatary.presentation.scene.show

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.edition.EditExperienceActivity
import com.pachatary.presentation.profile.ProfileActivity
import com.pachatary.presentation.scene.edition.CreateSceneActivity
import com.pachatary.presentation.scene.edition.EditSceneActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_experience_scenes.*


class ExperienceScenesActivity : AppCompatActivity(), ExperienceScenesView {

    @Inject
    lateinit var presenter: ExperienceScenesPresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var retryButton: ImageView

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private const val MAP_INTENT = 1
        private const val EXPERIENCE_ID = "experienceId"
        private const val SHOW_EDITABLE_IF_ITS_MINE = "show_editable_if_its_mine"

        fun newIntent(context: Context, experienceId: String,
                      showEditableIfItsMine: Boolean = false): Intent {
            val intent = Intent(context, ExperienceScenesActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SHOW_EDITABLE_IF_ITS_MINE, showEditableIfItsMine)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience_scenes)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.scenes_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        retryButton = findViewById(R.id.retry)
        retryButton.setOnClickListener { presenter.onRetryClick() }

        PachataryApplication.injector.inject(this)
        presenter.setView(view = this, experienceId = intent.getStringExtra(EXPERIENCE_ID))
        registry.addObserver(presenter)

        recyclerView.adapter = ExperienceScenesAdapter(layoutInflater,
                intent.getBooleanExtra(SHOW_EDITABLE_IF_ITS_MINE, false), presenter)
    }

    override fun navigateToEditScene(sceneId: String, experienceId: String) {
        startActivity(EditSceneActivity.newIntent(this, experienceId, sceneId))
    }

    override fun navigateToEditExperience(experienceId: String) {
        startActivity(EditExperienceActivity.newIntent(this, experienceId))
    }

    override fun showExperience(experience: Experience) {
        recyclerView.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE

        (recyclerView.adapter as ExperienceScenesAdapter).experience = experience
        (recyclerView.adapter as ExperienceScenesAdapter).isExperienceInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showScenes(scenes: List<Scene>) {
        recyclerView.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE

        (recyclerView.adapter as ExperienceScenesAdapter).scenes = scenes
        (recyclerView.adapter as ExperienceScenesAdapter).areScenesInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showLoadingExperience() {
        recyclerView.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE

        (recyclerView.adapter as ExperienceScenesAdapter).isExperienceInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showLoadingScenes() {
        recyclerView.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE

        (recyclerView.adapter as ExperienceScenesAdapter).areScenesInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showRetry() {
        recyclerView.visibility = View.INVISIBLE
        retryButton.visibility = View.VISIBLE
    }

    override fun navigateToExperienceMap(experienceId: String, showSceneWithId: String?) {
        startActivityForResult(ExperienceMapActivity.newIntent(this, experienceId, showSceneWithId),
                               MAP_INTENT)
    }

    override fun navigateToCreateScene(experienceId: String) {
        startActivity(CreateSceneActivity.newIntent(this, experienceId))
    }

    override fun showUnsaveDialog() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title_unsave_experience)
                .setMessage(R.string.dialog_question_unsave_experience)
                .setPositiveButton(android.R.string.yes, { _, _ -> presenter.onConfirmUnsaveExperience() })
                .setNegativeButton(android.R.string.no, { _, _ -> presenter.onCancelUnsaveExperience() })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun showSavedMessage() {
        val message = this.resources.getString(R.string.message_experience_saved)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperienceScenesAdapter(private val inflater: LayoutInflater,
                                  val showEditable: Boolean,
                                  val presenter: ExperienceScenesPresenter)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val LOADER_TYPE = 0
        val EXPERIENCE_TYPE = 1
        val SCENE_TYPE = 2

        lateinit var recyclerView: RecyclerView
        var isExperienceInProgress = true
        var areScenesInProgress = true
        var experience: Experience? = null
        var scenes: List<Scene> = listOf()

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                EXPERIENCE_TYPE -> {
                    val experienceViewHolder = holder as ExperienceViewHolder
                    experienceViewHolder.bind(experience!!)
                }
                SCENE_TYPE -> {
                    val sceneViewHolder = holder as SceneViewHolder
                    sceneViewHolder.bind(scenes[position-1])
                }
            }
        }

        override fun getItemCount(): Int {
            if (isExperienceInProgress && areScenesInProgress) return 1
            else if (areScenesInProgress) return 2
            else return scenes.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                if (isExperienceInProgress) return LOADER_TYPE
                else return EXPERIENCE_TYPE
            }
            else {
                if (areScenesInProgress) return SCENE_TYPE
                else return SCENE_TYPE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                EXPERIENCE_TYPE -> {
                    return ExperienceViewHolder(inflater.inflate(R.layout.item_experience, parent, false),
                            showEditable, { presenter.onEditExperienceClick() },
                            { presenter.onAddSceneButtonClick() },
                            { save -> presenter.onExperienceSave(save) },
                            { presenter.onMapButtonClick() },
                            { presenter.onProfileClick(it) })
                }
                SCENE_TYPE -> {
                    var isEditable = false
                    if (showEditable && experience != null && experience!!.isMine) isEditable = true

                    return SceneViewHolder(inflater.inflate(R.layout.item_scene, parent, false),
                            isEditable , { presenter.onEditSceneClick(it) },
                            { presenter.onLocateSceneClick(it) })
                }
                else -> {
                    return object : RecyclerView.ViewHolder(
                            inflater.inflate(R.layout.item_loader, parent, false)) {}
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
                val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                    override fun getVerticalSnapPreference(): Int {
                        return LinearSmoothScroller.SNAP_TO_START
                    }
                }
                smoothScroller.targetPosition = selectedPosition + 1
                Handler().postDelayed({
                    try {
                        this.recyclerView.layoutManager.startSmoothScroll(smoothScroller)
                    } catch (e: Exception) {
                    }
                }, 100)
            }
        }
    }

    class ExperienceViewHolder(view: View, private val showEditableIfItsMine: Boolean,
                               private val onEditExperienceClick: () -> Unit,
                               private val onAddSceneClick: () -> Unit,
                               private val onSaveExperienceClick: (Boolean) -> Unit,
                               private val onMapButtonClick: () -> Unit,
                               private val onProfileClick: (String) -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

        private val titleView: TextView = view.findViewById(R.id.title)
        private val descriptionView: TextView = view.findViewById(R.id.description)
        private val pictureView: ImageView = view.findViewById(R.id.picture)
        private val editButton: FloatingActionButton = view.findViewById(R.id.edit_button)
        private val addSceneButton: FloatingActionButton = view.findViewById(R.id.add_scene_button)
        private val saveButton: FloatingActionButton = view.findViewById(R.id.save_button)
        private val unsaveButton: FloatingActionButton = view.findViewById(R.id.unsave_button)
        private val savesCountView: TextView = view.findViewById(R.id.saves_count)
        private val authorUsernameView: TextView = view.findViewById(R.id.author_username)
        private val authorPictureView: ImageView = view.findViewById(R.id.author_picture)
        private val mapButton: Button = view.findViewById(R.id.map_button)
        lateinit var experienceId: String

        fun bind(experience: Experience) {
            this.experienceId = experience.id
            titleView.text = experience.title
            descriptionView.text = experience.description

            editButton.setOnClickListener { onEditExperienceClick() }
            addSceneButton.setOnClickListener { onAddSceneClick() }
            saveButton.setOnClickListener { onSaveExperienceClick(true) }
            unsaveButton.setOnClickListener { onSaveExperienceClick(false) }
            if (showEditableIfItsMine && experience.isMine) {
                editButton.visibility = View.VISIBLE
                addSceneButton.visibility = View.VISIBLE
                saveButton.visibility = View.GONE
                unsaveButton.visibility = View.GONE
            }
            else if (!experience.isMine) {
                addSceneButton.visibility = View.GONE
                editButton.visibility = View.GONE
                if (experience.isSaved) {
                    saveButton.visibility = View.INVISIBLE
                    unsaveButton.visibility = View.VISIBLE
                }
                else {
                    saveButton.visibility = View.VISIBLE
                    unsaveButton.visibility = View.INVISIBLE
                }
            }
            else {
                addSceneButton.visibility = View.GONE
                editButton.visibility = View.GONE
                saveButton.visibility = View.GONE
                unsaveButton.visibility = View.GONE
            }

            authorPictureView.setOnClickListener {
                onProfileClick(experience.authorProfile.username) }
            authorUsernameView.setOnClickListener {
                onProfileClick(experience.authorProfile.username) }
            savesCountView.text = experience.savesCount.toString() + " ★"
            authorUsernameView.text = experience.authorProfile.username
            Picasso.with(pictureView.context)
                    .load(experience.picture?.mediumUrl)
                    .into(pictureView)
            Picasso.with(authorPictureView.context)
                    .load(experience.authorProfile.picture?.tinyUrl)
                    .transform(CropCircleTransformation())
                    .into(authorPictureView)

            mapButton.setOnClickListener { onMapButtonClick() }
        }
    }

    class SceneViewHolder(view: View, private val isEditable: Boolean,
                          private val onEditSceneClick: (String) -> Unit,
                          private val onLocateSceneClick: (String) -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

        private val titleView: TextView = view.findViewById(R.id.title)
        private val descriptionView: TextView = view.findViewById(R.id.description)
        private val pictureView: ImageView = view.findViewById(R.id.picture)
        private val editButton: FloatingActionButton = view.findViewById(R.id.edit_button)
        private val locateButton: ImageButton = view.findViewById(R.id.locate_button)
        lateinit var sceneId: String

        fun bind(scene: Scene) {
            this.sceneId = scene.id
            titleView.text = scene.title
            descriptionView.text = scene.description
            editButton.setOnClickListener { onEditSceneClick(this.sceneId) }
            locateButton.setOnClickListener { onLocateSceneClick(this.sceneId) }
            if (isEditable) editButton.visibility = View.VISIBLE
            else editButton.visibility = View.GONE
            Picasso.with(pictureView.context)
                    .load(scene.picture?.mediumUrl)
                    .into(pictureView)
        }
    }
}
