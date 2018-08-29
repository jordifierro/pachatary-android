package com.pachatary.presentation.experience.edition

import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.view.LoaderView

interface EditExperienceView : LoaderView {
    fun showExperience(experience: Experience)
    fun finish()
    fun title(): String
    fun description(): String
    fun picture(): String?
    fun showTitleError()
    fun showDescriptionError()
    fun disableUpdateButton()
    fun enableUpdateButton()
    fun showError()
}