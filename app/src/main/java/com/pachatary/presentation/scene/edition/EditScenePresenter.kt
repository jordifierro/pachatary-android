package com.pachatary.presentation.scene.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class EditScenePresenter @Inject constructor(private val sceneRepository: SceneRepository,
                                             @Named("myexperiences") private val mainScheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var view: EditSceneView
    lateinit var experienceId: String
    lateinit var sceneId: String
    lateinit var scene: Scene
    var disposable: Disposable? = null
    var editDisposable: Disposable? = null

    fun setViewExperienceIdAndSceneId(view: EditSceneView, experienceId: String, sceneId: String) {
        this.view = view
        this.experienceId = experienceId
        this.sceneId = sceneId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        disposable = sceneRepository.sceneFlowable(experienceId, sceneId)
                .observeOn(mainScheduler)
                .take(1)
                .subscribe({ scene = it.data!!
                             view.showScene(it.data) },
                           { throw it })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
        editDisposable?.dispose()
    }

    fun onUpdateButtonClick() {
        if (view.title().isEmpty() || view.title().length > 80) view.showTitleError()
        else if (view.description().isEmpty()) view.showDescriptionError()
        else if (view.latitude() == null || view. longitude() == null) view.showLocationError()
        else updateScene()
    }

    private fun updateScene() {
        scene = Scene(scene.id, view.title(), view.description(), scene.picture,
                      view.latitude()!!, view.longitude()!!, scene.experienceId)
        editDisposable = sceneRepository.editScene(scene)
                .observeOn(mainScheduler)
                .subscribe({ when {
                    it.isInProgress() -> {
                        view.showLoader()
                        view.disableUpdateButton()
                    }
                    it.isSuccess() -> {
                        view.hideLoader()
                        onSceneEditedCorrectly()
                    }
                    it.isError() -> {
                        view.hideLoader()
                        view.enableUpdateButton()
                        view.showError()
                    }
                } }, { throw it })
    }

    private fun onSceneEditedCorrectly() {
        if (view.picture() != null) sceneRepository.uploadScenePicture(scene.id, view.picture()!!)
        view.finish()
    }
}