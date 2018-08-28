package com.pachatary.presentation.register

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ClientException
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class ConfirmEmailPresenter @Inject constructor(private val authRepository: AuthRepository,
                                                private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ConfirmEmailView

    @SuppressLint("CheckResult")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        confirmEmail()
    }

    fun onRetryClick() {
        confirmEmail()
    }

    @SuppressLint("CheckResult")
    private fun confirmEmail() {
        authRepository.confirmEmail(view.confirmationToken())
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({
                    when {
                        it.isSuccess() -> {
                            view.hideLoader()
                            view.showSuccessMessage()
                            view.navigateToMain()
                        }
                        it.isInProgress() -> view.showLoader()
                        else -> {
                            view.hideLoader()
                            if (it.error is ClientException) {
                                view.showInvalidTokenMessage()
                                view.navigateToRegisterWithDelay()
                            } else view.showRetry()
                        }
                    }
                }, { throw it })
    }
}
