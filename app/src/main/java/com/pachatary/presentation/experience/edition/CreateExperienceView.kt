package com.pachatary.presentation.experience.edition

import com.pachatary.presentation.common.view.LoaderView

interface CreateExperienceView : LoaderView {
    fun title(): String
    fun description(): String
    fun picture(): String?
    fun disableCreateButton()
    fun enableCreateButton()
    fun showTitleError()
    fun showDescriptionError()
    fun showPictureError()
    fun showError()
    fun finish()
}