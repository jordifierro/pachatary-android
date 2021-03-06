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
                                                 private val schedulerProvider: SchedulerProvider)
                                                                               : LifecycleObserver {

    lateinit var view: ExperienceMapView
    lateinit var experienceId: String
    var showSceneId: String? = null

    private var scenesDisposable: Disposable? = null

    fun setView(view: ExperienceMapView, experienceId: String, showSceneId: String? = null) {
        this.view = view
        this.experienceId = experienceId
        this.showSceneId = showSceneId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        connectToScenes()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        scenesDisposable?.dispose()
    }

    fun onSceneClick(sceneId: String) {
        view.finishWithSceneId(sceneId)
    }

    private fun connectToScenes() {
        view.showLoader()
        scenesDisposable = Flowable.combineLatest(
                mapLoadedFlowable(),
                scenesFlowable().filter { !it.isInProgress() },
                BiFunction { _: Any, scenesResult: Result<List<Scene>> -> scenesResult })
                .subscribe({ view.showScenesOnMap(it.data!!)
                             if (showSceneId != null) view.selectScene(showSceneId!!)
                    view.hideLoader() }, { throw it })
    }

    private fun mapLoadedFlowable() = view.mapLoadedFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())

    private fun scenesFlowable() = repository.scenesFlowable(experienceId)
                                             .subscribeOn(schedulerProvider.subscriber())
                                             .observeOn(schedulerProvider.observer())
}
