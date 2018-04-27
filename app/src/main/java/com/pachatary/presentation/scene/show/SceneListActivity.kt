package com.pachatary.presentation.scene.show

import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.edition.EditExperienceActivity
import com.pachatary.presentation.scene.edition.EditSceneActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_scene_list.*
import javax.inject.Inject


class SceneListActivity : AppCompatActivity(), SceneListView {

    @Inject
    lateinit var presenter: SceneListPresenter

    private lateinit var recyclerView: RecyclerView
    private var firstTime = true

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private const val EXPERIENCE_ID = "experienceId"
        private const val SELECTED_SCENE_ID = "selected_scene_id"
        private const val IS_MINE = "is_mine"

        fun newIntent(context: Context, experienceId: String,
                      selectedSceneId: String, isMine: Boolean): Intent {
            val intent = Intent(context, SceneListActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SELECTED_SCENE_ID, selectedSceneId)
            intent.putExtra(IS_MINE, isMine)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_list)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.scenes_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        PachataryApplication.injector.inject(this)
        presenter.setView(view = this,
                          experienceId = intent.getStringExtra(EXPERIENCE_ID),
                          selectedSceneId = intent.getStringExtra(SELECTED_SCENE_ID),
                          isMine = intent.getBooleanExtra(IS_MINE, false))
        registry.addObserver(presenter)
    }

    override fun showExperienceScenesAndScrollToSelectedIfFirstTime(experience: Experience,
                                                                    scenes: List<Scene>,
                                                                    selectedSceneId: String) {
        supportActionBar?.title = experience.title
        if (firstTime) {
            recyclerView.adapter = ExperienceSceneListAdapter(layoutInflater, experience.isMine,
                                                              experience, scenes, presenter)
            (recyclerView.adapter!! as ExperienceSceneListAdapter).scrollToSceneId(selectedSceneId)
            firstTime = false
        }
        else (recyclerView.adapter!! as ExperienceSceneListAdapter)
                .setExperienceAndScenes(experience, scenes)
    }

    override fun navigateToEditScene(sceneId: String, experienceId: String) {
        startActivity(EditSceneActivity.newIntent(this, experienceId, sceneId))
    }

    override fun navigateToEditExperience(experienceId: String) {
        startActivity(EditExperienceActivity.newIntent(this, experienceId))
    }

    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperienceSceneListAdapter(private val inflater: LayoutInflater, val isMine: Boolean,
                                     var experience: Experience, private var sceneList: List<Scene>,
                                     val presenter: SceneListPresenter)
        : RecyclerView.Adapter<ExperienceSceneViewHolder>() {

        lateinit var recyclerView: RecyclerView

        override fun onBindViewHolder(holder: ExperienceSceneViewHolder, position: Int) {
            if (position == 0) holder.bind(experience)
            else holder.bind(sceneList[position - 1])
        }

        override fun getItemCount() = sceneList.size + 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ExperienceSceneViewHolder(inflater.inflate(R.layout.item_experience_scene_list,
                    parent, false), isMine,
                    { presenter.onEditExperienceClick(it) }, {presenter.onEditSceneClick(it) })

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
        }

        fun scrollToSceneId(sceneId: String) {
            val selectedPosition = sceneList.indexOfFirst { it.id == sceneId }
            val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                override fun getVerticalSnapPreference(): Int {
                    return LinearSmoothScroller.SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = selectedPosition + 1
            Handler().postDelayed({
                try { this.recyclerView.layoutManager.startSmoothScroll(smoothScroller) }
                catch (e: Exception) {}
            }, 100)
        }

        fun setExperienceAndScenes(experience: Experience, scenes: List<Scene>) {
            this.experience = experience
            this.sceneList = scenes
            notifyDataSetChanged()
        }
    }

    class ExperienceSceneViewHolder(view: View, isMine: Boolean,
                                    private val onEditExperienceClick: (String) -> Unit,
                                    private val onEditSceneClick: (String) -> Unit)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView = view.findViewById(R.id.title)
        private val descriptionView: TextView = view.findViewById(R.id.description)
        private val pictureView: ImageView = view.findViewById(R.id.picture)
        private val editButton: FloatingActionButton = view.findViewById(R.id.edit_button)
        private val savesCountAndAuthorLayout: RelativeLayout =
                view.findViewById(R.id.saves_count_and_author)
        private val savesCountView: TextView = view.findViewById(R.id.saves_count)
        private val authorView: TextView = view.findViewById(R.id.author)
        private var isScene = true
        lateinit var experienceId: String
        lateinit var sceneId: String

        init {
            if (isMine) editButton.visibility = View.VISIBLE
            else editButton.visibility = View.INVISIBLE
        }

        fun bind(experience: Experience) {
            this.isScene = false
            this.experienceId = experience.id
            titleView.text = experience.title
            descriptionView.text = experience.description
            editButton.setOnClickListener { onEditButtonClick() }
            savesCountAndAuthorLayout.visibility = View.VISIBLE
            savesCountView.text = experience.savesCount.toString()
            authorView.text = "by " + experience.authorUsername
            Picasso.with(pictureView.context)
                    .load(experience.picture?.mediumUrl)
                    .into(pictureView)
        }

        fun bind(scene: Scene) {
            this.isScene = true
            this.sceneId = scene.id
            titleView.text = scene.title
            descriptionView.text = scene.description
            savesCountAndAuthorLayout.visibility = View.GONE
            editButton.setOnClickListener { onEditButtonClick() }
            Picasso.with(pictureView.context)
                    .load(scene.picture?.mediumUrl)
                    .into(pictureView)
        }

        override fun onClick(view: View?) {}

        private fun onEditButtonClick() {
            if (isScene) onEditSceneClick(sceneId)
            else onEditExperienceClick(experienceId)
        }
    }
}
