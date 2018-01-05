package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SavedPresenter @Inject constructor(private val repository: ExperienceRepository,
                                         private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SavedView

    private var experiencesDisposable: Disposable? = null

    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        view.hideRetry()
        view.showLoader()
        repository.refreshSavedExperiences()
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        view.showLoader()
        experiencesDisposable = repository.savedExperiencesFlowable()
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
                                          .subscribe({ view.hideLoader()
                                                       if (it.isSuccess()) view.showExperienceList(it.data!!)
                                                       else view.showRetry() })
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
