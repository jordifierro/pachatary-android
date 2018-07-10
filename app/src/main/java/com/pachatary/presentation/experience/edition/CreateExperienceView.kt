package com.pachatary.presentation.experience.edition

interface CreateExperienceView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectImage()
    fun finish()
}