package com.abidria.presentation.scene.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import com.abidria.presentation.common.view.edition.SelectLocationPresenter
import javax.inject.Inject

class EditScenePresenter @Inject constructor(private val sceneRepository: SceneRepository,
                                             private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var view: EditSceneView
    lateinit var experienceId: String
    lateinit var sceneId: String
    lateinit var scene: Scene

    fun setView(view: EditSceneView, experienceId: String, sceneId: String) {
        this.view = view
        this.experienceId = experienceId
        this.sceneId = sceneId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        sceneRepository.sceneFlowable(experienceId, sceneId)
                .observeOn(schedulerProvider.observer())
                .subscribeOn(schedulerProvider.subscriber())
                .take(1)
                .subscribe { result -> scene = result.data!!
                                       view.navigateToEditTitleAndDescription(scene.title, scene.description) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        scene = Scene(scene.id, title, description, scene.picture, scene.latitude, scene.longitude, scene.experienceId)
        view.navigateToSelectLocation(scene.latitude, scene.longitude, SelectLocationPresenter.LocationType.SPECIFIC)
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        scene = Scene(scene.id, scene.title, scene.description, scene.picture, latitude, longitude, scene.experienceId)
        sceneRepository.editScene(scene)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe { onSceneEditedCorrectly(it.data!!) }
    }

    fun onSelectLocationCanceled() {
        view.navigateToEditTitleAndDescription(scene.title, scene.description)
    }

    fun onSceneEditedCorrectly(scene: Scene) {
        this.scene = scene
        view.askUserToEditPicture()
    }

    fun onAskUserEditPictureResponse(userWantsToEditPicture: Boolean) {
        if (userWantsToEditPicture) view.navigateToPickImage()
        else view.finish()
    }

    fun onImagePicked(selectedImageUriString: String) {
        view.navigateToCropImage(selectedImageUriString)
    }

    fun onPickImageCanceled() {
        view.finish()
    }

    fun onImageCropped(croppedImageUriString: String) {
        sceneRepository.uploadScenePicture(scene.id, croppedImageUriString)
        view.finish()
    }

    fun onCropImageCanceled() {
        view.navigateToPickImage()
    }
}