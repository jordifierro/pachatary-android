package com.pachatary.presentation.scene.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ExperienceScenesPresenter @Inject constructor(
        private val sceneRepository: SceneRepository,
        private val experienceRepository: ExperienceRepository,
        private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ExperienceScenesView
    lateinit var experienceId: String

    private var scenesDisposable: Disposable? = null
    private var experienceDisposable: Disposable? = null

    fun setView(view: ExperienceScenesView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        loadExperienceAndScenes()
    }

    private fun loadExperienceAndScenes() {
        getExperience()
        getScenes()
    }

    private fun getExperience() {
        experienceDisposable = experienceRepository.experienceFlowable(experienceId)
                .observeOn(schedulerProvider.observer())
                .subscribe { if (it.isSuccess()) view.showExperience(it.data!!)
                             else if (it.isInProgress()) view.showLoadingExperience()
                             else view.showRetry() }
    }

    private fun getScenes() {
        scenesDisposable = sceneRepository.scenesFlowable(experienceId)
                .observeOn(schedulerProvider.observer())
                .subscribe { if (it.isSuccess()) view.showScenes(it.data!!)
                             else if (it.isInProgress()) view.showLoadingScenes()
                             else view.showRetry() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        experienceDisposable?.dispose()
        scenesDisposable?.dispose()
    }

    fun onEditExperienceClick() {
        view.navigateToEditExperience(experienceId)
    }

    fun onEditSceneClick(sceneId: String) {
        view.navigateToEditScene(sceneId, experienceId)
    }

    fun onExperienceSave(save: Boolean) {
        if (save) {
            experienceRepository.saveExperience(experienceId, true)
            view.showSavedMessage()
        }
        else (view.showUnsaveDialog())
    }

    fun onRetryClick() {
        loadExperienceAndScenes()
    }

    fun onMapButtonClick() {
        view.navigateToExperienceMap(experienceId)
    }

    fun onConfirmUnsaveExperience() {
        experienceRepository.saveExperience(this.experienceId, false)
    }

    fun onCancelUnsaveExperience() {}

    fun onSceneSelectedOnMap(selectedSceneId: String) {
        view.scrollToScene(selectedSceneId)
    }

    fun onLocateSceneClick(sceneId: String) {
        view.navigateToExperienceMap(experienceId, sceneId)
    }

    fun onAddSceneButtonClick() {
        view.navigateToCreateScene(experienceId)
    }

    fun onProfileClick(username: String) {
        view.navigateToProfile(username)
    }
}
