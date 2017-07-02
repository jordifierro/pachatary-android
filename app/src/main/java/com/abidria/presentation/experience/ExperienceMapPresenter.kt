package com.abidria.presentation.experience

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.experience.ExperienceRepository
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class ExperienceMapPresenter @Inject constructor(private val repository: SceneRepository,
                                                 private val experienceRepository: ExperienceRepository,
                                                 private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ExperienceMapView
    lateinit var experienceId: String

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
        experienceRepository.getExperience(experienceId)
                            .subscribeOn(schedulerProvider.subscriber())
                            .observeOn(schedulerProvider.observer())
                            .subscribe({ experience -> view.setTitle(experience.title)})
    }

    private fun setScenesOnMap() {
        Flowable.zip(mapLoadedFlowable(),
                     scenesFlowable(),
                     BiFunction { success: Boolean, scenes: List<Scene> -> scenes })
                .subscribe({ scenes -> view.showScenesOnMap(scenes) })
    }

    private fun mapLoadedFlowable() = view.mapLoadedFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())

    private fun scenesFlowable() = repository.getScenes(experienceId)
                                             .subscribeOn(schedulerProvider.subscriber())
                                             .observeOn(schedulerProvider.observer())
}
