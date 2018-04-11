package com.pachatary.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.experience.NewExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MyExperiencesPresenter @Inject constructor(private val newExperiencesRepository: NewExperienceRepository,
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
        newExperiencesRepository.getFirstExperiences(NewExperienceRepository.Kind.MINE)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        experiencesDisposable =
                newExperiencesRepository.experiencesFlowable(NewExperienceRepository.Kind.MINE)
                                        .subscribeOn(schedulerProvider.subscriber())
                                        .observeOn(schedulerProvider.observer())
                                        .subscribe({  if (it.isInProgress()) view.showLoader()
                                                      else view.hideLoader()

                                                      if (it.isError()) view.showRetry()
                                                      else view.hideRetry()

                                                      if (it.isSuccess())
                                                          view.showExperienceList(it.data!!)
                                                   })
        newExperiencesRepository.getFirstExperiences(NewExperienceRepository.Kind.MINE)
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
