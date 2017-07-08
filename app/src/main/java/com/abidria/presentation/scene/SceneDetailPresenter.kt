package com.abidria.presentation.scene

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class SceneDetailPresenter @Inject constructor(private val repository: SceneRepository,
                                               private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SceneDetailView
    lateinit var experienceId: String
    lateinit var sceneId: String

    fun setView(view: SceneDetailView, experienceId: String, sceneId: String) {
        this.view = view
        this.experienceId = experienceId
        this.sceneId = sceneId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        repository.getScene(experienceId = experienceId, sceneId = sceneId)
                  .subscribeOn(schedulerProvider.subscriber())
                  .observeOn(schedulerProvider.observer())
                  .subscribe({ scene -> view.showScene(scene) })
    }
}
