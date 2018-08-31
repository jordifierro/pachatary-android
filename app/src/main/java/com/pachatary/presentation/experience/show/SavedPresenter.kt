package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class SavedPresenter @Inject constructor(private val repository: ExperienceRepository,
                                         @Named("myexperiences") val scheduler: Scheduler)
                                                                               : LifecycleObserver {

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
                                                  view.showLoader()
                                                  if (it.action == Request.Action.GET_FIRSTS)
                                                      view.showExperienceList(listOf())
                                                  else if (it.action == Request.Action.PAGINATE)
                                                      view.showExperienceList(it.data!!)
                                              }
                                              else view.hideLoader()

                                              if (it.isError() &&
                                                      it.action == Request.Action.GET_FIRSTS)
                                                  view.showRetry()

                                              if (it.isSuccess()) {
                                                  if (it.data!!.isEmpty())
                                                      view.showNoSavedExperiencesInfo()
                                                  else view.showExperienceList(it.data)
                                              }
                                          }, { throw it })
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.SAVED)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
