package com.pachatary.presentation.scene.show

import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene

interface ExperienceScenesView {

    fun navigateToEditScene(sceneId: String, experienceId: String)
    fun navigateToEditExperience(experienceId: String)
    fun showExperience(experience: Experience)
    fun showScenes(scenes: List<Scene>)
    fun showLoadingExperience()
    fun showLoadingScenes()
    fun showRetry()
    fun navigateToExperienceMap(experienceId: String, showSceneWithId: String? = null)
    fun showUnsaveDialog()
    fun showSavedMessage()
    fun scrollToScene(sceneId: String)
    fun navigateToCreateScene(experienceId: String)
    fun navigateToProfile(username: String)
    fun finish()
}
