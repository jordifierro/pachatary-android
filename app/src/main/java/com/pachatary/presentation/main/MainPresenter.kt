package com.pachatary.presentation.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class MainPresenter @Inject constructor(private val authRepository: AuthRepository,
                                        private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: MainView
    val viewsStack = mutableListOf<MainView.ExperiencesViewType>()

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
        view.selectTab(MainView.ExperiencesViewType.SAVED)
    }

    fun onTabClick(viewType: MainView.ExperiencesViewType) {
        if (authRepository.hasPersonCredentials()) {
            viewsStack.removeAll { it == viewType }
            viewsStack += viewType
            view.showView(viewType)
        }
    }

    fun onBackPressed() {
        viewsStack.removeAt(viewsStack.lastIndex)
        if (viewsStack.isEmpty()) view.finish()
        else view.selectTab(viewsStack.last())
    }
}
