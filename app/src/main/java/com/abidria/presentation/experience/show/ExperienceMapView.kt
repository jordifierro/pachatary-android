package com.abidria.presentation.experience.show

import com.abidria.data.scene.Scene
import com.abidria.presentation.common.view.LoaderView
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
