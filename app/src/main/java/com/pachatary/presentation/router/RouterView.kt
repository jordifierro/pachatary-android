package com.pachatary.presentation.router

import com.pachatary.presentation.common.view.LoaderView

interface RouterView : LoaderView {

    fun showRetryView()
    fun navigateToExperience(experienceId: String)
    fun navigateToProfile(username: String)
    fun finish()
}
