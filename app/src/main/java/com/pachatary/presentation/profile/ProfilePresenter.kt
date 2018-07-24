package com.pachatary.presentation.profile

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.profile.ProfileRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class ProfilePresenter @Inject constructor(private val repository: ExperienceRepository,
                                           private val profileRepo: ProfileRepository,
                                           @Named("main") val mainScheduler: Scheduler)
                                                                               : LifecycleObserver {

    lateinit var view: ProfileView
    lateinit var username: String

    private var experiencesDisposable: Disposable? = null
    private var profileDisposable: Disposable? = null

    fun setViewAndUsername(view: ProfileView, username: String) {
        this.view = view
        this.username = username
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        connectToProfile()
        connectToExperiences()
    }

    fun onRetryClick() {
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.PERSONS,
                                       Request.Params(username = this.username))
        connectToProfile()
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperienceWithFinishOnProfileClick(experienceId)
    }

    fun lastExperienceShown() {
        repository.getMoreExperiences(ExperienceRepoSwitch.Kind.PERSONS)
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.PERSONS)
                                          .observeOn(mainScheduler)
                                          .subscribe({
                                              if (it.isInProgress()) {
                                                  if (it.action == Request.Action.GET_FIRSTS) {
                                                      view.showExperiencesLoader()
                                                      view.hidePaginationLoader()
                                                  }
                                                  else if (it.action == Request.Action.PAGINATE) {
                                                      view.hideExperiencesLoader()
                                                      view.showPaginationLoader()
                                                  }
                                              }
                                              else {
                                                  view.hideExperiencesLoader()
                                                  view.hidePaginationLoader()
                                              }

                                              if (it.isError() &&
                                                      it.action == Request.Action.GET_FIRSTS)
                                                  view.showExperiencesRetry()
                                              else view.hideExperiencesRetry()

                                              if (it.isSuccess())
                                                  view.showExperienceList(it.data!!)
                                          }, { throw it })
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.PERSONS,
                                       Request.Params(username = this.username))
    }

    private fun connectToProfile() {
        profileDisposable = profileRepo.profile(this.username)
                .observeOn(mainScheduler)
                .subscribe({
                    if (it.isSuccess()) {
                        view.showProfile(it.data!!)
                        view.hideProfileLoader()
                        view.hideProfileRetry()
                    }
                    else if (it.isInProgress()) {
                        view.showProfileLoader()
                        view.hideProfileRetry()
                    }
                    else {
                        view.showProfileRetry()
                        view.hideProfileLoader()
                    }
                }, { throw it })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        experiencesDisposable?.dispose()
        profileDisposable?.dispose()
    }
}
