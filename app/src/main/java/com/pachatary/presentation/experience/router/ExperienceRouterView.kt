package com.pachatary.presentation.experience.router

import com.pachatary.presentation.common.view.LoaderView

interface ExperienceRouterView : LoaderView {

    fun showErrorMessage()
    fun showRetryView()
    fun hideRetryView()
    fun navigateToExperience(experienceId: String)
    fun finish()
}
