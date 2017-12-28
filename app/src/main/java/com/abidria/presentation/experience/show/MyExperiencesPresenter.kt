package com.abidria.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.auth.AuthRepository
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MyExperiencesPresenter @Inject constructor(private val experiencesRepository: ExperienceRepository,
                                                 private val authRepository: AuthRepository,
                                                 private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: MyExperiencesView

    private var experiencesDisposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (authRepository.canPersonCreateContent()) connectToExperiences()
        else view.showRegisterDialog()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        if (authRepository.canPersonCreateContent() && experiencesDisposable == null) connectToExperiences()
    }

    fun onRetryClick() {
        view.hideRetry()
        view.showLoader()
        experiencesRepository.refreshMyExperiences()
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        view.showLoader()
        experiencesDisposable = experiencesRepository.myExperiencesFlowable()
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
        if (authRepository.canPersonCreateContent()) view.navigateToCreateExperience()
        else view.showRegisterDialog()
    }

    fun onProceedToRegister() {
        view.navigateToRegister()
    }

    fun onDontProceedToRegister() {}
}
