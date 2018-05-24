package com.pachatary.presentation.main

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class MainPresenter @Inject constructor(private val authRepository: AuthRepository,
                                        @Named("main") val mainScheduler: Scheduler)
                                                                            : LifecycleObserver {

    lateinit var view: MainView
    val viewsStack = mutableListOf<MainView.ExperiencesViewType>()

    @SuppressLint("CheckResult")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (!authRepository.hasPersonCredentials()) {
            view.navigateToWelcome()
            view.finish()
        }
        else {
            view.selectTab(MainView.ExperiencesViewType.SAVED)
            authRepository.currentVersionHasExpired()
                    .observeOn(mainScheduler)
                    .subscribe { hasExpired -> if (hasExpired == true) view.showUpgradeDialog() }
        }
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

    fun onUpgradeDialogClick() {
        view.navigateToUpgradeApp()
    }

    fun onSettingsClick() {
        view.navigateToSettings()
    }
}
