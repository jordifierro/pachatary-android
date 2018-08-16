package com.pachatary.presentation.experience.show

import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.LoaderView

interface SavedView : LoaderView {

    fun showRetry()
    fun showExperienceList(experienceList: List<Experience>)
    fun showNoSavedExperiencesInfo()
    fun navigateToExperience(experienceId: String)
}
