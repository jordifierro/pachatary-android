package com.abidria.presentation.experience

import com.abidria.data.experience.Experience

interface ExperienceListView {
    fun showLoader()
    fun hideLoader()
    fun showRetry()
    fun hideRetry()
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
}
