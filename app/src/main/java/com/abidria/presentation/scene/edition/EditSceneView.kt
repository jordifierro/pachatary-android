package com.abidria.presentation.scene.edition

import com.abidria.presentation.common.edition.SelectLocationPresenter

interface EditSceneView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                 locationType: SelectLocationPresenter.LocationType)
    fun navigateToPickImage()
    fun navigateToCropImage(selectedImageUriString: String)
    fun askUserToEditPicture()
    fun finish()
}