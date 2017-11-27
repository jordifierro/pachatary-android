package com.abidria.presentation.scene.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SceneDetailPresenter @Inject constructor(private val repository: SceneRepository,
                                               private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SceneDetailView
    lateinit var experienceId: String
    lateinit var sceneId: String

    private var sceneDisposable: Disposable? = null

    fun setView(view: SceneDetailView, experienceId: String, sceneId: String) {
        this.view = view
        this.experienceId = experienceId
        this.sceneId = sceneId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        sceneDisposable = repository.sceneFlowable(experienceId = experienceId, sceneId = sceneId)
                                    .subscribeOn(schedulerProvider.subscriber())
                                    .observeOn(schedulerProvider.observer())
                                    .subscribe({ view.showScene(it.data!!) })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        sceneDisposable?.dispose()
    }

    fun onEditSceneClick() {
        view.navigateToEditScene(sceneId, experienceId)
    }
}
