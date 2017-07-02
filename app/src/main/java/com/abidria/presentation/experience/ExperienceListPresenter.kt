package com.abidria.presentation.experience

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class ExperienceListPresenter @Inject constructor(private val experienceRepository: ExperienceRepository,
                                                  private val schedulerProvider: SchedulerProvider)
                                                                                                : LifecycleObserver {

    lateinit var experienceListView: ExperienceListView

    fun setView(experienceListView: ExperienceListView) {
        this.experienceListView = experienceListView
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        experienceRepository.getExperiences()
                            .subscribeOn(schedulerProvider.subscriber())
                            .observeOn(schedulerProvider.observer())
                            .subscribe({ experiences -> experienceListView.showExperienceList(experiences) })
    }

    fun onExperienceClick(experienceId: String) {
        experienceListView.navigateToExperience(experienceId)
    }
}
