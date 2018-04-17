package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.common.Result
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class SavedPresenter @Inject constructor(private val repository: ExperienceRepository,
                                         @Named("main") val scheduler: Scheduler) : LifecycleObserver {

    lateinit var view: SavedView

    private var experiencesDisposable: Disposable? = null

    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.SAVED)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    fun lastExperienceShown() {
        repository.getMoreExperiences(ExperienceRepoSwitch.Kind.SAVED)
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.SAVED)
                                          .observeOn(scheduler)
                                          .subscribe({
                                              if (it.isInProgress()) {
                                                  if (it.lastEvent == Result.Event.GET_FIRSTS) {
                                                      view.showLoader()
                                                      view.hidePaginationLoader()
                                                  }
                                                  else if (it.lastEvent == Result.Event.PAGINATE) {
                                                      view.hideLoader()
                                                      view.showPaginationLoader()
                                                  }
                                              }
                                              else {
                                                  view.hideLoader()
                                                  view.hidePaginationLoader()
                                              }

                                              if (it.isError() &&
                                                      it.lastEvent == Result.Event.GET_FIRSTS)
                                                  view.showRetry()
                                              else view.hideRetry()

                                              if (it.isSuccess())
                                                  view.showExperienceList(it.data!!)
                                          })
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.SAVED)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
