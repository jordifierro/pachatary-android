package com.pachatary.presentation.scene.show

import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene

interface SceneListView {

    fun showExperienceScenesAndScrollToSelectedIfFirstTime(experience: Experience, scenes: List<Scene>,
                                                           selectedSceneId: String)
    fun navigateToEditScene(sceneId: String, experienceId: String)
    fun navigateToEditExperience(experienceId: String)
}
