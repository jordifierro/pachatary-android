package com.pachatary.presentation.scene.edition

import com.pachatary.presentation.common.view.LoaderView

interface CreateSceneView : LoaderView{
    fun title(): String
    fun description(): String
    fun picture(): String?
    fun latitude(): Double?
    fun longitude(): Double?
    fun disableCreateButton()
    fun enableCreateButton()
    fun showTitleError()
    fun showDescriptionError()
    fun showPictureError()
    fun showLocationError()
    fun showError()
    fun finish()
}