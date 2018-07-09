package com.pachatary.presentation.scene.show

import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene

interface SceneListView {

    fun navigateToEditScene(sceneId: String, experienceId: String)
    fun navigateToEditExperience(experienceId: String)
    fun showExperience(experience: Experience)
    fun showScenes(scenes: List<Scene>)
    fun showLoadingExperience()
    fun showLoadingScenes()
    fun showRetry()
    fun navigateToExperienceMap(experienceId: String)
    fun showUnsaveDialog()
    fun showSavedMessage()
    fun scrollToScene(sceneId: String)
}
