package com.abidria.presentation.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.auth.AuthRepository
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MainPresenter @Inject constructor(private val authRepository: AuthRepository,
                                        private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: MainView

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (authRepository.hasPersonCredentials()) goToInitTab()
        else {
            view.showLoader()
            view.showTabs(false)
            getPersonInvitation()
        }
    }

    private fun getPersonInvitation() {
        authRepository.getPersonInvitation()
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe { goToInitTab() }
    }

    private fun goToInitTab() {
        view.hideLoader()
        view.showTabs(true)
        view.showView(MainView.ExperiencesViewType.EXPLORE)
    }

    fun onTabClick(viewType: MainView.ExperiencesViewType) {
        if (authRepository.hasPersonCredentials()) view.showView(viewType)
    }
}
