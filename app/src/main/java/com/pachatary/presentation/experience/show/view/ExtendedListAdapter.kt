package com.pachatary.presentation.experience.show.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pachatary.R
import com.pachatary.data.experience.Experience

class ExtendedListAdapter(private val inflater: LayoutInflater,
                          var experienceList: List<Experience>,
                          var inProgress: Boolean,
                          val onClick: (String) -> Unit,
                          private val onLastItemShown: () -> Unit)
                                                : RecyclerView.Adapter<ExtendedViewHolder>() {

    override fun onBindViewHolder(holder: ExtendedViewHolder, position: Int) {
        if (inProgress && position == experienceList.size) holder.bindProgressBar()
        else {
            val endHasBeenReached = position == experienceList.size - 1
            if (experienceList.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            holder.bind(experienceList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtendedViewHolder {
        return ExtendedViewHolder(inflater.inflate(R.layout.item_extended_experience_list,
                parent, false), onClick)
    }

    override fun getItemCount(): Int {
        if (inProgress) return experienceList.size + 1
        return experienceList.size
    }
}
