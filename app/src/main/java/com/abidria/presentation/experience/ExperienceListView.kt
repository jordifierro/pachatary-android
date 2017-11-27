package com.abidria.presentation.experience

import com.abidria.data.experience.Experience
import com.abidria.presentation.common.view.LoaderView

interface ExperienceListView : LoaderView {

    fun showRetry()
    fun hideRetry()
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
    fun navigateToCreateExperience()
}
