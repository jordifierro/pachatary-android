package com.pachatary.presentation.profile

import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.view.LoaderView

interface ProfileView {

    fun showExperienceList(experienceList: List<Experience>)
    fun showExperiencesLoader()
    fun hideExperiencesLoader()
    fun showPaginationLoader()
    fun hidePaginationLoader()
    fun showExperiencesRetry()
    fun hideExperiencesRetry()

    fun showProfile(profile: Profile)
    fun showProfileLoader()
    fun hideProfileLoader()
    fun showProfileRetry()
    fun hideProfileRetry()

    fun navigateToExperienceWithFinishOnProfileClick(experienceId: String)
}
