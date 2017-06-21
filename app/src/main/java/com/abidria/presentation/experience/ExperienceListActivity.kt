package com.abidria.presentation.experience

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.abidria.R
import com.abidria.data.experience.Experience
import com.abidria.presentation.common.AbidriaApplication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_experiences_list.*
import javax.inject.Inject

class ExperienceListActivity : AppCompatActivity(), LifecycleRegistryOwner, ExperienceListView {

    @Inject
    lateinit var presenter: ExperienceListPresenter

    lateinit var recyclerView: RecyclerView

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experiences_list)
        setSupportActionBar(toolbar)

        recyclerView = findViewById<RecyclerView>(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        AbidriaApplication.injector.inject(this)
        presenter.setView(this)
        registry.addObserver(presenter)
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        recyclerView.adapter = ExperiencesListAdapter(layoutInflater, experienceList)
    }

    override fun navigateToExperience(experienceId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLifecycle(): LifecycleRegistry = registry

    class ExperiencesListAdapter(val inflater: LayoutInflater, val experienceList: List<Experience>) :
                                                                        RecyclerView.Adapter<ExperienceViewHolder>() {

        override fun onBindViewHolder(holder: ExperienceViewHolder?, position: Int) {
            holder?.bind(experienceList[position])
        }

        override fun getItemCount(): Int = experienceList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ExperienceViewHolder {
            return ExperienceViewHolder(inflater.inflate(R.layout.experiences_list_item, parent, false))
        }
    }

    class ExperienceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val titleView: TextView
        private val pictureView: ImageView

        init {
            titleView = view.findViewById<TextView>(R.id.experience_title)
            pictureView = view.findViewById<ImageView>(R.id.experience_picture)
        }

        fun bind(experience: Experience) {
            titleView.text = experience.title
            Picasso.with(pictureView.context)
                   .load(experience.picture?.small)
                   .into(pictureView)
        }
    }
}
