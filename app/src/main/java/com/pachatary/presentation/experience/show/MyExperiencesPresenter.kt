package com.pachatary.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MyExperiencesPresenter @Inject constructor(
        private val experiencesRepository: ExperienceRepository,
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
        if (authRepository.canPersonCreateContent() && experiencesDisposable == null)
            connectToExperiences()
    }

    fun onRetryClick() {
        experiencesRepository.getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        experiencesDisposable =
                experiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE)
                                        .subscribeOn(schedulerProvider.subscriber())
                                        .observeOn(schedulerProvider.observer())
                                        .subscribe({
                                            if (it.isInProgress()) {
                                                if (it.action == Request.Action.GET_FIRSTS) {
                                                    view.showLoader()
                                                    view.showExperienceList(listOf())
                                                    view.hidePaginationLoader()
                                                }
                                                else if (it.action == Request.Action.PAGINATE) {
                                                    view.hideLoader()
                                                    view.showPaginationLoader()
                                                }
                                            }
                                            else {
                                                view.hideLoader()
                                                view.hidePaginationLoader()
                                            }

                                            if (it.isError() &&
                                                    it.action == Request.Action.GET_FIRSTS)
                                                    view.showRetry()
                                            else view.hideRetry()

                                            if (it.isSuccess())
                                                view.showExperienceList(it.data!!)
                                        }, { throw it })
        experiencesRepository.getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    fun lastExperienceShown() {
        experiencesRepository.getMoreExperiences(ExperienceRepoSwitch.Kind.MINE)
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
