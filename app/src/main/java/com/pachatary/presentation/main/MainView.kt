package com.pachatary.presentation.main

interface MainView {

    enum class ExperiencesViewType {
        MY_EXPERIENCES, SAVED, EXPLORE
    }

    fun selectTab(type: ExperiencesViewType)
    fun showView(viewType: ExperiencesViewType)
    fun finish()
    fun navigateToWelcome()
}
