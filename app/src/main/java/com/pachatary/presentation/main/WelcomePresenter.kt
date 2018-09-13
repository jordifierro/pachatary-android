package com.pachatary.presentation.main

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class WelcomePresenter @Inject constructor(private val authRepository: AuthRepository,
                                           @Named("main") private val mainScheduler: Scheduler)
                                                                            : LifecycleObserver {

    lateinit var view: WelcomeView

    fun onStartClick() {
        getPersonInvitation()
    }

    @SuppressLint("CheckResult")
    private fun getPersonInvitation() {
        authRepository.getPersonInvitation()
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.navigateToMain()
                        view.finish()
                    }
                    else if (it.isError()) {
                        view.showErrorMessage()
                        view.hideLoader()
                        view.enableButtons()
                    }
                    else if (it.isInProgress()) {
                        view.showLoader()
                        view.disableButtons()
                    }
                }
    }

    fun onLoginClick() {
        view.navigateToAskLogin()
    }

    fun onPrivacyPolicyClick() {
        view.navigateToPrivacyPolicy()
    }

    fun onTermsAndConditionsClick() {
        view.navigateToTermsAndConditions()
    }
}
