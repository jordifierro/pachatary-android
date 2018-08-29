package com.pachatary.presentation.scene.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import io.reactivex.disposables.Disposable
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class CreateScenePresenter @Inject constructor(private val sceneRepository: SceneRepository,
                                               @Named("main") private val mainScheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var experienceId: String
    lateinit var view: CreateSceneView
    var disposable: Disposable? = null

    fun setViewAndExperienceId(view: CreateSceneView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onCreateButtonClick() {
        if (view.title().isEmpty() || view.title().length > 80) view.showTitleError()
        else if (view.description().isEmpty()) view.showDescriptionError()
        else if (view.picture() == null) view.showPictureError()
        else if (view.latitude() == null || view.longitude() == null) view.showLocationError()
        else createScene()
    }

    private fun createScene() {
        val sceneToCreate = Scene(id = "", title = view.title(), description = view.description(),
                latitude = view.latitude()!!, longitude = view.longitude()!!,
                experienceId = experienceId, picture = null)
        disposable = sceneRepository.createScene(sceneToCreate)
                .observeOn(mainScheduler)
                .subscribe({ when {
                    it.isInProgress() -> {
                        view.showLoader()
                        view.disableCreateButton()
                    }
                    it.isSuccess() -> {
                        view.hideLoader()
                        onSceneCreatedCorrectly(it.data!!)
                    }
                    it.isError() -> {
                        view.hideLoader()
                        view.enableCreateButton()
                        view.showError()
                    }
                } }, { throw it })
    }

    private fun onSceneCreatedCorrectly(scene: Scene) {
        sceneRepository.uploadScenePicture(scene.id, view.picture()!!)
        view.finish()
    }
}