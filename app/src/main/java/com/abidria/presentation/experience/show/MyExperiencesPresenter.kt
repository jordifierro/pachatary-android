package com.abidria.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MyExperiencesPresenter @Inject constructor(private val repository: ExperienceRepository,
                                                 private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: MyExperiencesView

    private var experiencesDisposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        view.hideRetry()
        view.showLoader()
        repository.refreshMyExperiences()
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        view.showLoader()
        experiencesDisposable = repository.myExperiencesFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
                                          .subscribe({ view.hideLoader()
                                                       if (it.isSuccess()) view.showExperienceList(it.data!!)
                                                       else view.showRetry() })
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }

    fun onCreateExperienceClick() {
        view.navigateToCreateExperience()
    }
}
