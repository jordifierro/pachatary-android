package com.pachatary.presentation.login

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class AskLoginEmailPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        @Named("main") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: AskLoginEmailView

    @SuppressLint("CheckResult")
    fun onAskClick(email: String) {
        authRepository.askLoginEmail(email)
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.showSuccessMessage()
                        view.finish()
                    }
                    else if (it.isError()) {
                        view.showErrorMessage()
                        view.enableAskButton()
                        view.hideLoader()
                    }
                    else if (it.isInProgress()) {
                        view.showLoader()
                        view.disableAskButton()
                    }
                }
    }
}
