package com.abidria.presentation.scene.create

interface CreateSceneView {
    fun navigateToEditTitleAndDescription()
    fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                 locationType: SelectLocationPresenter.LocationType)
    fun navigateToPickImage()
    fun navigateToCropImage(selectedImageUriString: String)
    fun finish()
}