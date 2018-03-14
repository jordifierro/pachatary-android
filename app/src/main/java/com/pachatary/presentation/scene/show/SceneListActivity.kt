package com.pachatary.presentation.scene.show

import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_scene_list.*
import javax.inject.Inject
import android.support.v7.widget.LinearSmoothScroller




class SceneListActivity : AppCompatActivity(), SceneListView {

    @Inject
    lateinit var presenter: SceneListPresenter

    lateinit var recyclerView: RecyclerView

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experienceId"
        private val SELECTED_SCENE_ID = "selected_scene_id"
        private val IS_MINE = "is_mine"

        fun newIntent(context: Context, experienceId: String, selectedSceneId: String, isMine: Boolean): Intent {
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

    override fun showExperienceScenesAndScrollToSelected(experience: Experience, scenes: List<Scene>,
                                                         selectedSceneId: String) {
        supportActionBar?.title = experience.title
        recyclerView.adapter = ExperienceSceneListAdapter(layoutInflater, experience, scenes)
        (recyclerView.adapter!! as ExperienceSceneListAdapter).scrollToSceneId(selectedSceneId)
    }


    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperienceSceneListAdapter(val inflater: LayoutInflater, val experience: Experience,
                                     val sceneList: List<Scene>) : RecyclerView.Adapter<ExperienceSceneViewHolder>() {

        lateinit var recyclerView: RecyclerView

        override fun onBindViewHolder(holder: ExperienceSceneViewHolder?, position: Int) {
            if (position == 0) holder?.bind(experience)
            else holder?.bind(sceneList[position - 1])
        }

        override fun getItemCount() = sceneList.size + 1

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
            ExperienceSceneViewHolder(inflater.inflate(R.layout.item_experience_scene_list, parent, false))

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView!!
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
    }

    class ExperienceSceneViewHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView
        private val descriptionView: TextView
        private val pictureView: ImageView
        private var isScene = true
        lateinit var experienceId: String
        lateinit var sceneId: String

        init {
            titleView = view.findViewById<TextView>(R.id.title)
            descriptionView = view.findViewById<TextView>(R.id.description)
            pictureView = view.findViewById<ImageView>(R.id.picture)
            view.setOnClickListener(this)
        }

        fun bind(experience: Experience) {
            this.isScene = false
            this.experienceId = experience.id
            titleView.text = experience.title
            descriptionView.text = experience.description
            Picasso.with(pictureView.context)
                    .load(experience.picture?.mediumUrl)
                    .into(pictureView)
        }

        fun bind(scene: Scene) {
            this.isScene = true
            this.experienceId = scene.id
            titleView.text = scene.title
            descriptionView.text = scene.description
            Picasso.with(pictureView.context)
                    .load(scene.picture?.mediumUrl)
                    .into(pictureView)
        }

        override fun onClick(view: View?) {}
    }
}
