package com.pachatary.presentation.experience.show

import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.LoaderView

interface MyExperiencesView : LoaderView {

    fun showRetry()
    fun hideRetry()
    fun showPaginationLoader()
    fun hidePaginationLoader()
    fun showExperienceList(experienceList: List<Experience>)
    fun showRegisterDialog()
    fun navigateToExperience(experienceId: String)
    fun navigateToCreateExperience()
    fun navigateToRegister()
}
