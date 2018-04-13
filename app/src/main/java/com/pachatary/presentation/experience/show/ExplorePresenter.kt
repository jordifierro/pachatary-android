package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ExplorePresenter @Inject constructor(private val repository: ExperienceRepository,
                                           private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: ExploreView

    private var experiencesDisposable: Disposable? = null

    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE)
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
                                          .subscribe({  if (it.isInProgress()) view.showLoader()
                                                        else view.hideLoader()

                                                        if (it.isError()) view.showRetry()
                                                        else view.hideRetry()

                                                        if (it.isSuccess())
                                                            view.showExperienceList(it.data!!)
                                                      })
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
