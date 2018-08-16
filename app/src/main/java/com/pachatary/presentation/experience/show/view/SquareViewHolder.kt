package com.pachatary.presentation.experience.show.view

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.squareup.picasso.Picasso

class SquareViewHolder(view: View, val onClick: (String) -> Unit,
                       val pictureDeviceCompat: PictureDeviceCompat)
    : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val titleView: TextView = view.findViewById(R.id.experience_title)
    private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
    lateinit var experienceId: String

    init { view.setOnClickListener(this) }

    fun bind(experience: Experience) {
        this.experienceId = experience.id
        titleView.text = experience.title
        Picasso.with(pictureView.context)
                .load(pictureDeviceCompat.convert(experience.picture)?.halfScreenSizeUrl)
                .into(pictureView)
    }

    override fun onClick(view: View?) = this.onClick(this.experienceId)
}
