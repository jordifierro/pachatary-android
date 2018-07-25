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
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ExtendedViewHolder(view: View,
                         val pictureDeviceCompat: PictureDeviceCompat,
                         val onClick: (String) -> Unit,
                         val onUsernameClick: (String) -> Unit)
    : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val titleView: TextView = view.findViewById(R.id.experience_title)
    private val authorUsernameView: TextView = view.findViewById(R.id.experience_author_username)
    private val authorPictureView: ImageView = view.findViewById(R.id.experience_author_picture)
    private val descriptionView: TextView = view.findViewById(R.id.experience_description)
    private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
    private val progressBar: ProgressBar = view.findViewById(R.id.experience_progressbar)
    private val savesCountView: TextView = view.findViewById(R.id.experience_saves_count)
    lateinit var experienceId: String

    init { view.setOnClickListener(this) }

    fun bind(experience: Experience) {
        titleView.visibility = View.VISIBLE
        pictureView.visibility = View.VISIBLE
        authorUsernameView.visibility = View.VISIBLE
        descriptionView.visibility = View.VISIBLE
        savesCountView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        this.experienceId = experience.id
        titleView.text = experience.title
        authorUsernameView.text = experience.authorProfile.username
        authorUsernameView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onUsernameClick.invoke(experience.authorProfile.username)
            }
        })
        authorPictureView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onUsernameClick.invoke(experience.authorProfile.username)
            }
        })
        descriptionView.text = experience.description
        savesCountView.text = experience.savesCount.toString() + " ★"
        Picasso.with(pictureView.context)
                .load(pictureDeviceCompat.convert(experience.picture)?.fullScreenSizeUrl)
                .into(pictureView)
        Picasso.with(authorPictureView.context)
                .load(pictureDeviceCompat.convert(experience.authorProfile.picture)?.iconSizeUrl)
                .transform(CropCircleTransformation())
                .into(authorPictureView)
    }

    fun bindProgressBar() {
        titleView.visibility = View.GONE
        pictureView.visibility = View.GONE
        authorUsernameView.visibility = View.GONE
        descriptionView.visibility = View.GONE
        savesCountView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) = this.onClick(this.experienceId)
}
