package com.pachatary.presentation.register

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class ConfirmEmailPresenter @Inject constructor(private val authRepository: AuthRepository,
                                                private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ConfirmEmailView

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.showLoader()
        authRepository.confirmEmail(view.confirmationToken())
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({
                    view.hideLoader()
                    if (it.isSuccess()) {
                        view.showMessage("Email successfully confirmed!")
                        view.navigateToMain()
                    } else {
                        view.showMessage(it.error!!.message!!)
                        view.finish()
                    }
                })
    }
}
