package com.abidria.presentation.scene.create

interface CreateSceneView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                 locationType: SelectLocationPresenter.LocationType)
    fun navigateToPickImage()
    fun navigateToCropImage(selectedImageUriString: String)
    fun finish()
}