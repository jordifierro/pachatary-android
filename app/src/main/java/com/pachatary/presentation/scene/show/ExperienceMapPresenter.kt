package com.pachatary.presentation.scene.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.common.Result
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class ExperienceMapPresenter @Inject constructor(private val repository: SceneRepository,
                                                 private val experienceRepository: ExperienceRepository,
                                                 private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ExperienceMapView
    lateinit var experienceId: String
    var isExperienceMine = false

    private var experienceDisposable: Disposable? = null
    private var scenesDisposable: Disposable? = null

    fun setView(view: ExperienceMapView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        connectToExperience()
        connectToScenes()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        experienceDisposable?.dispose()
        scenesDisposable?.dispose()
    }

    fun onSceneClick(sceneId: String) {
        view.navigateToScene(experienceId = experienceId, isExperienceMine = isExperienceMine, sceneId = sceneId)
    }

    fun onCreateSceneClick() {
        view.navigateToCreateScene(this.experienceId)
    }

    fun onSaveExperienceClick() {
        experienceRepository.saveExperience(experienceId = experienceId, save = true)
        view.showSavedMessage()
    }

    fun onUnsaveExperienceClick() {
        view.showUnsaveDialog()
    }

    fun onEditExperienceClick() {
        view.navigateToEditExperience(experienceId)
    }

    fun onConfirmUnsaveExperience() {
        experienceRepository.saveExperience(experienceId = experienceId, save = false)
    }

    fun onCancelUnsaveExperience() {}

    private fun connectToExperience() {
        experienceDisposable = experienceRepository.experienceFlowable(experienceId)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ if (it.isSuccess()) {
                    isExperienceMine = it.data!!.isMine
                    view.setTitle(it.data.title)
                    if (it.data.isMine) view.showEditButton()
                    else view.showSaveButton(isSaved = it.data.isSaved)
                }})
    }

    private fun connectToScenes() {
        view.showLoader()
        scenesDisposable = Flowable.combineLatest(
                mapLoadedFlowable(),
                scenesFlowable(),
                BiFunction { _: Any, scenesResult: Result<List<Scene>> -> scenesResult })
                .subscribe({ view.showScenesOnMap(it.data!!)
                    view.hideLoader() })
    }

    private fun mapLoadedFlowable() = view.mapLoadedFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())

    private fun scenesFlowable() = repository.scenesFlowable(experienceId)
                                             .subscribeOn(schedulerProvider.subscriber())
                                             .observeOn(schedulerProvider.observer())
}
