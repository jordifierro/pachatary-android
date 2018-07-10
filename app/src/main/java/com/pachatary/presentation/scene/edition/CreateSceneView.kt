package com.pachatary.presentation.scene.edition

import com.pachatary.presentation.common.edition.SelectLocationPresenter

interface CreateSceneView {
    fun navigateToEditTitleAndDescription(initialTitle: String = "", initialDescription: String = "")
    fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                 locationType: SelectLocationPresenter.LocationType)
    fun navigateToSelectImage()
    fun finish()
}