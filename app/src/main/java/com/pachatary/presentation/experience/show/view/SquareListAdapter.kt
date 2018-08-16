package com.pachatary.presentation.experience.show.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.PictureDeviceCompat

class SquareListAdapter(private val inflater: LayoutInflater,
                        val pictureDeviceCompat: PictureDeviceCompat,
                        var experienceList: List<Experience>,
                        var inProgress: Boolean,
                        var noSavedExperiences: Boolean,
                        val onClick: (String) -> Unit,
                        private val onLastItemShown: () -> Unit)
                                                : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val EXPERIENCE_TYPE = 1
    private val LOADER_TYPE = 2
    private val NO_SAVED_EXPERIENCES_TYPE = 3

    override fun getItemCount(): Int {
        if (noSavedExperiences) return 1
        if (inProgress) return experienceList.size + 1
        return experienceList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (noSavedExperiences) return NO_SAVED_EXPERIENCES_TYPE
        if (inProgress && position == experienceList.size) return LOADER_TYPE
        return EXPERIENCE_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NO_SAVED_EXPERIENCES_TYPE ->
                object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_no_saved_experiences, parent, false)) {}
            EXPERIENCE_TYPE ->
                return SquareViewHolder(inflater.inflate(R.layout.item_square_experiences_list,
                        parent, false), onClick, pictureDeviceCompat)
            else ->
                object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!(inProgress && position == experienceList.size) && !noSavedExperiences) {
            val endHasBeenReached = position == experienceList.size - 1
            if (experienceList.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            (holder as SquareViewHolder).bind(experienceList[position])
        }
    }
}
