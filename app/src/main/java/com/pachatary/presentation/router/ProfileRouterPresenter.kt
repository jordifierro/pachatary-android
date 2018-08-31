package com.pachatary.presentation.router

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class ProfileRouterPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        @Named("myexperiences") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: RouterView
    private lateinit var profileUsername: String
    var disposable: Disposable? = null

    fun setViewAndUsername(view: RouterView, username: String) {
        this.view = view
        this.profileUsername = username
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        getCredentialsAndNavigateToProfile()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onRetryClick() {
        getCredentialsAndNavigateToProfile()
    }

    private fun getCredentialsAndNavigateToProfile() {
        if (authRepository.hasPersonCredentials()) {
            view.navigateToProfile(profileUsername)
            view.finish()
        }
        else getPersonInvitation()
    }

    private fun getPersonInvitation() {
        disposable = authRepository.getPersonInvitation()
                .observeOn(mainScheduler)
                .subscribe({
                    when {
                        it.isSuccess() -> {
                            view.hideLoader()
                            view.navigateToProfile(profileUsername)
                            view.finish()
                        }
                        it.isError() -> {
                            view.hideLoader()
                            view.showRetryView()
                        }
                        it.isInProgress() -> view.showLoader()
                    }
                }, { throw it })
    }
}
