package com.pachatary.presentation.scene.edition

import com.pachatary.presentation.common.edition.SelectLocationPresenter

interface EditSceneView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                 locationType: SelectLocationPresenter.LocationType)
    fun navigateToSelectImage()
    fun askUserToEditPicture()
    fun finish()
}