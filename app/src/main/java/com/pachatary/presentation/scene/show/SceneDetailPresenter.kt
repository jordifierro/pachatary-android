package com.pachatary.presentation.scene.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SceneDetailPresenter @Inject constructor(private val repository: SceneRepository,
                                               private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SceneDetailView
    lateinit var experienceId: String
    lateinit var sceneId: String
    var isMine = false

    private var sceneDisposable: Disposable? = null

    fun setView(view: SceneDetailView, experienceId: String, sceneId: String, isMine: Boolean) {
        this.view = view
        this.experienceId = experienceId
        this.sceneId = sceneId
        this.isMine = isMine
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (isMine) view.showEditButton()
        else view.hideEditButton()

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
