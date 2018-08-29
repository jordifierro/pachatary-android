package com.pachatary.presentation.scene.edition

import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.view.LoaderView

interface EditSceneView : LoaderView {
    fun showScene(scene: Scene)
    fun title(): String
    fun description(): String
    fun picture(): String?
    fun latitude(): Double?
    fun longitude(): Double?
    fun disableUpdateButton()
    fun enableUpdateButton()
    fun showTitleError()
    fun showDescriptionError()
    fun showLocationError()
    fun showError()
    fun finish()
}