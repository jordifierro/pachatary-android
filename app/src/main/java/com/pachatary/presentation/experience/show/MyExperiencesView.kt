package com.pachatary.presentation.experience.show

import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile

interface MyExperiencesView {

    fun showExperienceList(experiences: List<Experience>)
    fun showNoExperiencesInfo()
    fun showExperiencesRetry()
    fun showExperiencesLoader()
    fun hideExperiencesLoader()
    fun showPaginationLoader()
    fun hidePaginationLoader()

    fun showProfile(profile: Profile)
    fun hideProfileLoader()
    fun showProfileLoader()
    fun showProfileRetry()

    fun showShareDialog(username: String)
    fun showNotEnoughInfoToShareDialog()

    fun navigateToExperience(experienceId: String)
    fun navigateToCreateExperience()
    fun navigateToRegister()
    fun navigateToPickAndCropImage()
}
