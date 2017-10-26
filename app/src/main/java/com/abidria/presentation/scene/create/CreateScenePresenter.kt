package com.abidria.presentation.scene.create

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class CreateScenePresenter @Inject constructor(private val sceneRepository: SceneRepository,
                                               private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var experienceId: String
    lateinit var view: CreateSceneView
    var title = ""
    var description = ""
    var latitude = 0.0
    var longitude = 0.0
    var createdScene: Scene? = null

    fun setView(view: CreateSceneView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.navigateToEditTitleAndDescription()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        this.title = title
        this.description = description
        view.navigateToSelectLocation()
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        val sceneToCreate = Scene(id = "", title = title, description = description,
                                  latitude = latitude, longitude = longitude,
                                  experienceId = experienceId, picture = null)
        sceneRepository.createScene(sceneToCreate)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ onSceneCreatedCorrectly(it.data!!) })
    }

    fun onSelectLocationCanceled() {
        view.navigateToEditTitleAndDescription()
    }

    fun onSceneCreatedCorrectly(scene: Scene) {
        this.createdScene = scene
        view.navigateToPickImage()
    }

    fun onImagePicked(selectedImageUriString: String) {
        view.navigateToCropImage(selectedImageUriString)
    }

    fun onPickImageCanceled() {
        view.finish()
    }

    fun onImageCropped(croppedImageUriString: String) {
        view.uploadImage(createdScene!!.id, croppedImageUriString)
        view.finish()
    }

    fun onCropImageCanceled() {
        view.navigateToPickImage()
    }
}