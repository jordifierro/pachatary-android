package com.pachatary.presentation.login

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ClientException
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class LoginPresenter @Inject constructor(private val authRepository: AuthRepository,
                                         @Named("myexperiences") private val mainScheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var view: LoginView

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        login()
    }

    fun retryClick() {
        login()
    }

    @SuppressLint("CheckResult")
    private fun login() {
        authRepository.login(view.loginToken())
                .observeOn(mainScheduler)
                .subscribe({
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.showSuccessMessage()
                        view.navigateToMain()
                        view.finish()
                    } else if (it.isError()) {
                        view.hideLoader()
                        if (it.error is ClientException) {
                            view.showErrorMessage()
                            view.navigateToAskLoginEmailWithDelay()
                            view.finishWithDelay()
                        }
                        else view.showRetry()
                    } else if (it.isInProgress()) {
                        view.showLoader()
                    }
                }, { throw it })
    }
}
