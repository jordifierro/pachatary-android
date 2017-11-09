package com.abidria.presentation.scene.create

interface CreateSceneView {
    fun navigateToEditTitleAndDescription()
    fun navigateToSelectLocation()
    fun navigateToPickImage()
    fun navigateToCropImage(selectedImageUriString: String)
    fun finish()
}