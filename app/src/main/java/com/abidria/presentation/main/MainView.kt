package com.abidria.presentation.main

import com.abidria.presentation.common.view.LoaderView

interface MainView : LoaderView {

    enum class ExperiencesViewType {
        MY_EXPERIENCES, SAVED, EXPLORE
    }

    fun showView(viewType: ExperiencesViewType)
    fun showTabs(visible: Boolean)
}
