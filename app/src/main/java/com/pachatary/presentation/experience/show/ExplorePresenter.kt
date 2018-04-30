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
    val getFirstExperiencesPublishSubject = PublishSubject.create<Unit>()
    val searchParamsChangedPublishSubject = PublishSubject.create<Request.Params>()

    init {
        Flowable.combineLatest<Unit, Request.Params, Request.Params>(
                getFirstExperiencesPublishSubject.toFlowable(BackpressureStrategy.LATEST).replay(1).autoConnect(),
                searchParamsChangedPublishSubject.toFlowable(BackpressureStrategy.LATEST).replay(1).autoConnect(),
                BiFunction { _, params: Request.Params -> params })
                .subscribe { repository.getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE, it) }
    }

    lateinit var view: ExploreView

    private var experiencesDisposable: Disposable? = null

    fun create() {
        connectToExperiences()
    }

    fun onRetryClick() {
        getFirstExperiencesPublishSubject.onNext(Unit)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    fun lastExperienceShown() {
        repository.getMoreExperiences(ExperienceRepoSwitch.Kind.EXPLORE)
    }

    fun onLastLocationFound(latitude: Double, longitude: Double) {
        searchParamsChangedPublishSubject.onNext(Request.Params(null, latitude, longitude))
    }

    private fun connectToExperiences() {
        experiencesDisposable = repository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE)
                                          .subscribeOn(schedulerProvider.subscriber())
                                          .observeOn(schedulerProvider.observer())
                                          .subscribe({
                                              if (it.isInProgress()) {
                                                  if (it.action == Request.Action.GET_FIRSTS) {
                                                      view.showLoader()
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
        getFirstExperiencesPublishSubject.onNext(Unit)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
    }
}
