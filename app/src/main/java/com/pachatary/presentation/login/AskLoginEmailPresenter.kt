package com.pachatary.presentation.login

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class AskLoginEmailPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        @Named("main") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: AskLoginEmailView

    var disposable: Disposable? = null

    fun onAskClick(email: String) {
        disposable = authRepository.askLoginEmail(email)
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.showSuccessMessage()
                        view.finishApplication()
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }
}
