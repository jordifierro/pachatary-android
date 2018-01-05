package com.pachatary.presentation.scene.show

import com.pachatary.data.scene.Scene

interface SceneDetailView {

    fun showScene(scene: Scene)
    fun navigateToEditScene(sceneId: String, experienceId: String)
    fun showEditButton()
    fun hideEditButton()
}
