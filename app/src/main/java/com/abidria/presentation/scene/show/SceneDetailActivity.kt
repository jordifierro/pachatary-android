package com.abidria.presentation.scene.show

import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.abidria.R
import com.abidria.data.scene.Scene
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.scene.edition.EditSceneActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_experience_map.*
import javax.inject.Inject


class SceneDetailActivity : AppCompatActivity(), SceneDetailView {

    @Inject
    lateinit var presenter: SceneDetailPresenter

    lateinit var imageView: ImageView
    lateinit var textView: TextView
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout
    lateinit var editSceneButton: FloatingActionButton

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experienceId"
        private val SCENE_ID = "scene_id"
        private val IS_MINE = "is_mine"

        fun newIntent(context: Context, experienceId: String, sceneId: String, isMine: Boolean): Intent {
            val intent = Intent(context, SceneDetailActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SCENE_ID, sceneId)
            intent.putExtra(IS_MINE, isMine)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_detail)
        setSupportActionBar(toolbar)

        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        imageView = findViewById(R.id.scenes_image)
        textView = findViewById(R.id.scenes_text)
        editSceneButton = findViewById(R.id.edit_scene_button)
        editSceneButton.setOnClickListener { presenter.onEditSceneClick() }

        AbidriaApplication.injector.inject(this)
        presenter.setView(view = this,
                          experienceId = intent.getStringExtra(EXPERIENCE_ID),
                          sceneId = intent.getStringExtra(SCENE_ID),
                          isMine = intent.getBooleanExtra(IS_MINE, false))
        registry.addObserver(presenter)
    }

    override fun showScene(scene: Scene) {
        supportActionBar?.title = scene.title
        collapsingToolbarLayout.title = scene.title
        Picasso.with(this)
                .load(scene.picture?.mediumUrl)
                .into(imageView)
        textView.text = scene.description
    }

    override fun showEditButton() {
        editSceneButton.visibility = View.VISIBLE
    }

    override fun hideEditButton() {
        editSceneButton.visibility = View.INVISIBLE
    }

    override fun navigateToEditScene(sceneId: String, experienceId: String) {
        startActivity(EditSceneActivity.newIntent(this, experienceId, sceneId))
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
