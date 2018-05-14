package com.pachatary.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class PersonsExperiencesPresenter @Inject constructor(private val repository: ExperienceRepository,
                                                      @Named("main") val scheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var view: PersonsExperiencesView
    lateinit var username: String

    private var experiencesDisposable: Disposable? = null

    fun setViewAndUsername(view: PersonsExperiencesView, username: String) {
        this.view = view
        this.username = username
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.PERSONS,
                                       Request.Params(username = this.username))
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    fun lastExperienceShown() {
        repository.getMoreExperiences(ExperienceRepoSwitch.Kind.SAVED)
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.PERSONS)
                                          .observeOn(scheduler)
                                          .subscribe({
                                              if (it.isInProgress()) {
                                                  if (it.action == Request.Action.GET_FIRSTS) {
                                                      view.showLoader()
                                                      view.showExperienceList(listOf())
                                                      view.hidePaginationLoader()
                                                  }
                                                  else if (it.action == Request.Action.PAGINATE) {
                                                      view.hideLoader()
                                                      view.showPaginationLoader()
                                                  }
                                              }
                                              else {
                                                  view.hideLoader()
                                                  view.hidePaginationLoader()
                                              }

                                              if (it.isError() &&
                                                      it.action == Request.Action.GET_FIRSTS)
                                                  view.showRetry()
                                              else view.hideRetry()

                                              if (it.isSuccess())
                                                  view.showExperienceList(it.data!!)
                                          })
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.PERSONS,
                                       Request.Params(username = this.username))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
