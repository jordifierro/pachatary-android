package com.abidria.presentation.experience

import com.abidria.data.experience.Experience

interface ExperienceListView {
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
}
