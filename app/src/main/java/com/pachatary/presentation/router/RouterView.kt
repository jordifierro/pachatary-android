package com.pachatary.presentation.router

import com.pachatary.presentation.common.view.LoaderView

interface RouterView : LoaderView {

    fun showErrorMessage()
    fun showRetryView()
    fun hideRetryView()
    fun navigateToExperience(experienceId: String)
    fun navigateToProfile(username: String)
    fun finish()
}
