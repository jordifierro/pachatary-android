package com.pachatary.presentation.profile

import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.view.LoaderView

interface ProfileView : LoaderView {

    fun showRetry()
    fun hideRetry()
    fun showPaginationLoader()
    fun hidePaginationLoader()
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
    fun showProfile(profile: Profile)
}
