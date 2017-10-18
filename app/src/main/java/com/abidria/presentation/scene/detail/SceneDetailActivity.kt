package com.abidria.presentation.scene.detail

import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.abidria.R
import com.abidria.data.scene.Scene
import com.abidria.presentation.common.AbidriaApplication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_experience_map.*
import javax.inject.Inject


class SceneDetailActivity : AppCompatActivity(), SceneDetailView {

    @Inject
    lateinit var presenter: SceneDetailPresenter

    lateinit var imageView: ImageView
    lateinit var textView: TextView
    lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experience_id"
        private val SCENE_ID = "scene_id"

        fun newIntent(context: Context, experienceId: String, sceneId: String): Intent {
            val intent = Intent(context, SceneDetailActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SCENE_ID, sceneId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_detail)
        setSupportActionBar(toolbar)

        collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        imageView = findViewById<ImageView>(R.id.scenes_image)
        textView = findViewById<TextView>(R.id.scenes_text)

        AbidriaApplication.injector.inject(this)
        presenter.setView(view = this,
                          experienceId = intent.getStringExtra(EXPERIENCE_ID),
                          sceneId = intent.getStringExtra(SCENE_ID))
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

    override fun getLifecycle(): LifecycleRegistry = registry
}
