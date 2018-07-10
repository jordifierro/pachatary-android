package com.pachatary.presentation.experience.edition

interface EditExperienceView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectImage()
    fun askUserToEditPicture()
    fun finish()
}