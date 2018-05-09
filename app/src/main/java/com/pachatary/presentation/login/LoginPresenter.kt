package com.pachatary.presentation.login

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class LoginPresenter @Inject constructor(private val authRepository: AuthRepository,
                                         @Named("main") private val mainScheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var view: LoginView

    @SuppressLint("CheckResult")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
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
                        view.showErrorMessage()
                        view.finish()
                    } else if (it.isInProgress()) {
                        view.showLoader()
                    }
                })
    }
}
