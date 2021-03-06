package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ExplorePresenter @Inject constructor(private val repository: ExperienceRepository,
                                           private val schedulerProvider: SchedulerProvider)
                                                                               : LifecycleObserver {
    lateinit var view: ExploreView

    private var experiencesDisposable: Disposable? = null

    var latitude: Double? = null
    var longitude: Double? = null
    var searchText: String? = null

    fun create() {
        connectToExperiences()
        if (view.hasLocationPermission()) onPermissionsAccepted()
        else view.askPermissions()
    }

    fun onPermissionsAccepted() {
        view.askLastKnownLocation()
    }

    fun onPermissionsDenied() {
        getFirstsExperiences()
    }

    private fun getFirstsExperiences() {
        repository.getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                Request.Params(searchText, latitude, longitude))
    }

    fun onRetryClick() {
        getFirstsExperiences()
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    fun lastExperienceShown() {
        repository.getMoreExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                                      Request.Params(searchText, latitude, longitude))
    }

    fun onLastLocationFound(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        getFirstsExperiences()
    }

    fun onLastLocationNotFound() {
        getFirstsExperiences()
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE)
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
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

                                              if (it.isSuccess())
                                                  view.showExperienceList(it.data!!)
                                          }, { throw it })
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }

    fun onUsernameClicked(username: String) {
        view.navigateToPersonsExperiences(username)
    }

    fun searchClick(text: String) {
        searchText = text
        getFirstsExperiences()
    }

    fun locationClick() {
        view.navigateToSelectLocation(latitude, longitude)
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.searchText = view.searchText()
        getFirstsExperiences()
    }

    fun onRefresh() {
        getFirstsExperiences()
    }
}
