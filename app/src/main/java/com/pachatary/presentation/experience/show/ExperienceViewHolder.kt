package com.pachatary.presentation.experience.show

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.squareup.picasso.Picasso

class ExperienceViewHolder(view: View, val onClick: (String) -> Unit)
    : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val titleView: TextView = view.findViewById(R.id.experience_title)
    private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
    private val progressBar: ProgressBar = view.findViewById(R.id.experience_progressbar)
    lateinit var experienceId: String

    init {
        view.setOnClickListener(this)
    }

    fun bind(experience: Experience) {
        titleView.visibility = View.VISIBLE
        pictureView.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        this.experienceId = experience.id
        titleView.text = experience.title
        Picasso.with(pictureView.context)
                .load(experience.picture?.smallUrl)
                .into(pictureView)
    }

    fun bindProgressBar() {
        titleView.visibility = View.INVISIBLE
        pictureView.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) = this.onClick(this.experienceId)
}
