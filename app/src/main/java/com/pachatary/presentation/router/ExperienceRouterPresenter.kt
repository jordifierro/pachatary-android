package com.pachatary.presentation.router

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class ExperienceRouterPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        private val experienceRepository: ExperienceRepository,
        @Named("myexperiences") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: RouterView
    lateinit var experienceShareId: String
    var disposable: Disposable? = null

    fun setViewAndExperienceShareId(view: RouterView, experienceShareId: String) {
        this.view = view
        this.experienceShareId = experienceShareId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        getCredentialsAndTranslateShareId()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onRetryClick() {
        getCredentialsAndTranslateShareId()
    }

    private fun getCredentialsAndTranslateShareId() {
        if (authRepository.hasPersonCredentials()) translateExperienceShareId()
        else getPersonInvitation()
    }

    private fun getPersonInvitation() {
        disposable = authRepository.getPersonInvitation()
                .observeOn(mainScheduler)
                .subscribe({
                    when {
                        it.isSuccess() -> {
                            view.hideLoader()
                            translateExperienceShareId()
                        }
                        it.isError() -> {
                            view.hideLoader()
                            view.showRetryView()
                        }
                        it.isInProgress() -> view.showLoader()
                    }
                }, { throw it })
    }

    @SuppressLint("CheckResult")
    private fun translateExperienceShareId() {
        experienceRepository.translateShareId(experienceShareId)
                .observeOn(mainScheduler)
                .subscribe({
                    when {
                        it.isSuccess() -> {
                            view.hideLoader()
                            view.navigateToExperience(it.data!!)
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
