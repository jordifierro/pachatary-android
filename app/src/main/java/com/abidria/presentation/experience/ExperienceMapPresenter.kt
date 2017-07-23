package com.abidria.presentation.experience

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.common.Result
import com.abidria.data.experience.ExperienceRepository
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class ExperienceMapPresenter @Inject constructor(private val repository: SceneRepository,
                                                 private val experienceRepository: ExperienceRepository,
                                                 private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ExperienceMapView
    lateinit var experienceId: String

    private var experienceDisposable: Disposable? = null
    private var scenesDisposable: Disposable? = null

    fun setView(view: ExperienceMapView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        setExperienceTitle()
        setScenesOnMap()
    }

    private fun setExperienceTitle() {
        experienceDisposable = experienceRepository.experienceFlowable(experienceId)
                                                   .subscribeOn(schedulerProvider.subscriber())
                                                   .observeOn(schedulerProvider.observer())
                                                   .subscribe({ if (it.isSuccess()) view.setTitle(it.data!!.title)})
    }

    private fun setScenesOnMap() {
        view.showLoader()
        scenesDisposable = Flowable.zip(mapLoadedFlowable(),
                                        scenesFlowable(),
                                        BiFunction { _: Any, scenesResult: Result<List<Scene>> -> scenesResult })
                                   .subscribe({ view.showScenesOnMap(it.data!!)
                                                view.hideLoader() })
    }

    fun onSceneClick(sceneId: String) {
        view.navigateToScene(experienceId = experienceId, sceneId = sceneId)
    }

    private fun mapLoadedFlowable() = view.mapLoadedFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())

    private fun scenesFlowable() = repository.scenesFlowable(experienceId)
                                             .subscribeOn(schedulerProvider.subscriber())
                                             .observeOn(schedulerProvider.observer())

    fun destroy() {
        experienceDisposable?.dispose()
        scenesDisposable?.dispose()
    }
}
