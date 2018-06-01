package com.pachatary.presentation.experience.router

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class ExperienceRouterPresenter @Inject constructor(
        private val authRepository: AuthRepository,
        private val experienceRepository: ExperienceRepository,
        @Named("main") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: ExperienceRouterView
    lateinit var experienceShareId: String

    fun setViewAndExperienceShareId(view: ExperienceRouterView, experienceShareId: String) {
        this.view = view
        this.experienceShareId = experienceShareId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        getCredentialsAndTranslateShareId()
    }

    fun onRetryClick() {
        getCredentialsAndTranslateShareId()
    }

    private fun getCredentialsAndTranslateShareId() {
        if (authRepository.hasPersonCredentials()) translateExperienceShareId()
        else getPersonInvitation()
    }

    @SuppressLint("CheckResult")
    private fun getPersonInvitation() {
        authRepository.getPersonInvitation()
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.hideRetryView()
                        translateExperienceShareId()
                    }
                    else if (it.isError()) {
                        view.showErrorMessage()
                        view.hideLoader()
                        view.showRetryView()
                    }
                    else if (it.isInProgress()) {
                        view.showLoader()
                        view.hideRetryView()
                    }
                }
    }

    @SuppressLint("CheckResult")
    private fun translateExperienceShareId() {
        experienceRepository.translateShareId(experienceShareId)
                .observeOn(mainScheduler)
                .subscribe {
                    if (it.isSuccess()) {
                        view.hideLoader()
                        view.hideRetryView()
                        view.navigateToExperience(it.data!!)
                    }
                    else if (it.isError()) {
                        view.showErrorMessage()
                        view.hideLoader()
                        view.showRetryView()
                    }
                    else if (it.isInProgress()) {
                        view.showLoader()
                        view.hideRetryView()
                    }
                }
    }
}
