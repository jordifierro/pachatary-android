package com.abidria.presentation.scene.show

import com.abidria.data.scene.Scene

interface SceneDetailView {

    fun showScene(scene: Scene)
    fun navigateToEditScene(sceneId: String, experienceId: String)
}
