package com.pachatary.presentation.experience.show.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.PictureDeviceCompat

class ExtendedListAdapter(private val inflater: LayoutInflater,
                          val pictureDeviceCompat: PictureDeviceCompat,
                          var experienceList: List<Experience>,
                          var inProgress: Boolean,
                          val onClick: (String) -> Unit,
                          private val onUsernameClick: (String) -> Unit,
                          private val onLastItemShown: () -> Unit)
                                                : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val EXPERIENCE_TYPE = 1
    private val LOADER_TYPE = 2

    override fun getItemCount(): Int {
        if (inProgress) return experienceList.size + 1
        return experienceList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (inProgress && position == experienceList.size) return LOADER_TYPE
        return EXPERIENCE_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EXPERIENCE_TYPE ->
                ExtendedViewHolder(inflater.inflate(R.layout.item_extended_experience_list,
                        parent, false), pictureDeviceCompat, onClick, onUsernameClick)
            else ->
                object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!(inProgress && position == experienceList.size)) {
            val endHasBeenReached = position == experienceList.size - 1
            if (experienceList.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            (holder as ExtendedViewHolder).bind(experienceList[position])
        }
    }
}
