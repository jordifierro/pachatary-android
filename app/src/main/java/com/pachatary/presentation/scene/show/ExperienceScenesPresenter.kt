package com.pachatary.presentation.scene.show

import android.annotation.SuppressLint
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
    var finishOnProfileClick = false

    private var scenesDisposable: Disposable? = null
    private var experienceDisposable: Disposable? = null

    fun setView(view: ExperienceScenesView, experienceId: String, finishOnProfileClick: Boolean) {
        this.view = view
        this.experienceId = experienceId
        this.finishOnProfileClick = finishOnProfileClick
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
                .subscribe({ when {
                               it.isSuccess() -> view.showExperience(it.data!!)
                               it.isInProgress() -> view.showLoadingExperience()
                               else -> view.showRetry()
                           }}, { throw it })
    }

    private fun getScenes() {
        scenesDisposable = sceneRepository.scenesFlowable(experienceId)
                .observeOn(schedulerProvider.observer())
                .subscribe({ when {
                               it.isSuccess() -> view.showScenes(it.data!!)
                               it.isInProgress() -> view.showLoadingScenes()
                               else -> view.showRetry()
                           }}, { throw it })
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
        if (finishOnProfileClick) view.finish()
        else view.navigateToProfile(username)
    }

    @SuppressLint("CheckResult")
    fun onShareClick() {
        experienceRepository.getShareUrl(experienceId)
                .observeOn(schedulerProvider.observer())
                .subscribe({ when {
                    it.isSuccess() -> view.showShareDialog(it.data!!)
                    it.isInProgress() -> {}
                    else -> view.showError()
                }}, { throw it })
    }

    fun onRefresh() {
        sceneRepository.refreshScenes(experienceId)
        experienceRepository.refreshExperience(experienceId)
    }
}
