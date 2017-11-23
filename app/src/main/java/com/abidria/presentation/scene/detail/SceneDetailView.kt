package com.abidria.presentation.scene.detail

import com.abidria.data.scene.Scene

interface SceneDetailView {

    fun showScene(scene: Scene)
    fun navigateToEditScene(sceneId: String, experienceId: String)
}
