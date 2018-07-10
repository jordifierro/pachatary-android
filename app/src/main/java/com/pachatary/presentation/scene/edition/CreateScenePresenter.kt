package com.pachatary.presentation.scene.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CreateScenePresenter @Inject constructor(private val sceneRepository: SceneRepository,
                                               private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var experienceId: String
    lateinit var view: CreateSceneView
    var title = ""
    var description = ""
    var latitude = 0.0
    var longitude = 0.0
    var selectedImageUriString: String? = null
    var createdScene: Scene? = null
    var lastLocationFound = false
    var lastLatitude = 0.0
    var lastLongitude = 0.0
    var disposable: Disposable? = null

    fun setView(view: CreateSceneView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.navigateToEditTitleAndDescription()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onLastLocationFound(latitude: Double, longitude: Double) {
        lastLatitude = latitude
        lastLongitude = longitude
        lastLocationFound = true
    }

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        this.title = title
        this.description = description
        navigateToSelectLocation()
    }

    private fun navigateToSelectLocation() {
        var locationType = SelectLocationPresenter.LocationType.UNKNWON
        if (lastLocationFound) locationType = SelectLocationPresenter.LocationType.APROX
        view.navigateToSelectLocation(lastLatitude, lastLongitude, locationType)
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        onLastLocationFound(latitude, longitude)
        this.latitude = latitude
        this.longitude = longitude
        view.navigateToSelectImage()
    }

    fun onSelectLocationCanceled() {
        view.navigateToEditTitleAndDescription(title, description)
    }

    fun onSelectImageSuccess(selectedImageUriString: String) {
        this.selectedImageUriString = selectedImageUriString
        createScene()
    }

    fun onSelectImageCanceled() {
        navigateToSelectLocation()
    }

    private fun createScene() {
        val sceneToCreate = Scene(id = "", title = title, description = description,
                latitude = latitude, longitude = longitude,
                experienceId = experienceId, picture = null)
        disposable = sceneRepository.createScene(sceneToCreate)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ onSceneCreatedCorrectly(it.data!!) }, { throw it })
    }

    private fun onSceneCreatedCorrectly(scene: Scene) {
        uploadPicture(scene.id)
        view.finish()
    }

    private fun uploadPicture(id: String) {
        sceneRepository.uploadScenePicture(id, selectedImageUriString!!)
    }
}