package com.pachatary.presentation.main

import com.pachatary.presentation.common.view.LoaderView

interface MainView : LoaderView {

    enum class ExperiencesViewType {
        MY_EXPERIENCES, SAVED, EXPLORE
    }

    fun showView(viewType: ExperiencesViewType)
    fun showTabs(visible: Boolean)
}
