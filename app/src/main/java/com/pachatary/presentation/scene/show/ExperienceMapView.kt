package com.pachatary.presentation.scene.show

import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.view.LoaderView
import io.reactivex.Flowable

interface ExperienceMapView : LoaderView {

    fun showScenesOnMap(scenes: List<Scene>)
    fun mapLoadedFlowable(): Flowable<Any>
    fun setTitle(title: String)
    fun navigateToScene(experienceId: String, isExperienceMine: Boolean, sceneId: String)
    fun navigateToCreateScene(experienceId: String)
    fun navigateToEditExperience(experienceId: String)
    fun showEditButton()
    fun showSaveButton(isSaved: Boolean)
    fun showUnsaveDialog()
    fun showSavedMessage()
}
