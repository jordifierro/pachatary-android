package com.pachatary.presentation.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import javax.inject.Inject

class MainPresenter @Inject constructor(private val authRepository: AuthRepository)
                                                                            : LifecycleObserver {

    lateinit var view: MainView
    val viewsStack = mutableListOf<MainView.ExperiencesViewType>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (!authRepository.hasPersonCredentials()) {
            view.navigateToWelcome()
            view.finish()
        }
        else view.selectTab(MainView.ExperiencesViewType.SAVED)
    }

    fun onTabClick(viewType: MainView.ExperiencesViewType) {
        viewsStack.removeAll { it == viewType }
        viewsStack += viewType
        view.showView(viewType)
    }

    fun onBackPressed() {
        viewsStack.removeAt(viewsStack.lastIndex)
        if (viewsStack.isEmpty()) view.finish()
        else view.selectTab(viewsStack.last())
    }
}
