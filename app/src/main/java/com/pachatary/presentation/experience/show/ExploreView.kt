package com.pachatary.presentation.experience.show

import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.LoaderView

interface ExploreView : LoaderView {

    fun showRetry()
    fun showPaginationLoader()
    fun hidePaginationLoader()
    fun showExperienceList(experienceList: List<Experience>)
    fun navigateToExperience(experienceId: String)
    fun hasLocationPermission(): Boolean
    fun askLastKnownLocation()
    fun askPermissions()
    fun navigateToPersonsExperiences(username: String)
    fun navigateToSelectLocation(latitude: Double?, longitude: Double?)
    fun searchText(): String
}
