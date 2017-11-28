package com.abidria.presentation.experience.edition

interface EditExperienceView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToPickImage()
    fun navigateToCropImage(selectedImageUriString: String)
    fun askUserToEditPicture()
    fun finish()
}