package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.experience.NewExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SavedPresenter @Inject constructor(private val repository: NewExperienceRepository,
                                         private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SavedView

    private var experiencesDisposable: Disposable? = null

    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        repository.getFirstExperiences(NewExperienceRepository.Kind.SAVED)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(NewExperienceRepository.Kind.SAVED)
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
                                          .subscribe({  if (it.isInProgress()) view.showLoader()
                                                        else view.hideLoader()

                                                        if (it.isError()) view.showRetry()
                                                        else view.hideRetry()

                                                        if (it.isSuccess())
                                                            view.showExperienceList(it.data!!)
                                          })
        repository.getFirstExperiences(NewExperienceRepository.Kind.SAVED)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
