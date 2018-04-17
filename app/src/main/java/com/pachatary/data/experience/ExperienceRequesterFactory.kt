package com.pachatary.data.experience

import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class ExperienceRequesterFactory(val apiRepository: ExperienceApiRepository) {

    enum class Action {
        GET_FIRSTS, PAGINATE, REFRESH
    }

    fun create(resultCache: ResultCacheFactory.ResultCache<Experience>,
               kind: ExperienceRepoSwitch.Kind): Observer<Action> {
        val actionsSubject = PublishSubject.create<Action>()
        val disposable = actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultCache.resultFlowable,
                        BiFunction<Action, Result<List<Experience>>,
                                Pair<Action, Result<List<Experience>>>>
                        { action, result -> Pair(action, result) })
                .subscribe({
                    if (it.first == Action.GET_FIRSTS) {
                        if (!it.second.isInProgress() &&
                                (!it.second.hasBeenInitialized() || it.second.isError())) {
                            resultCache.replaceResultObserver.onNext(Result(listOf(),
                                    inProgress = true, lastEvent = Result.Event.GET_FIRSTS))
                            apiCallFlowable(apiRepository, kind).subscribe({ apiResult ->
                                resultCache.replaceResultObserver.onNext(
                                    apiResult.builder().lastEvent(Result.Event.GET_FIRSTS).build())
                            })
                        }
                    }
                    else if (it.first == Action.PAGINATE) {
                        if (!it.second.isInProgress() &&
                                (it.second.isSuccess() && it.second.hasBeenInitialized() ||
                            it.second.isError() && it.second.lastEvent == Result.Event.PAGINATE) &&
                                it.second.hasMoreElements()) {
                            resultCache.replaceResultObserver.onNext(
                                    it.second.builder()
                                                .inProgress(true)
                                                .lastEvent(Result.Event.PAGINATE)
                                                .error(null)
                                            .build())
                            apiRepository.paginateExperiences(it.second.nextUrl!!).subscribe(
                            { apiResult ->
                                val newResult =
                                    if (apiResult.isError()) {
                                        it.second.builder()
                                                .lastEvent(Result.Event.PAGINATE)
                                                .error(apiResult.error)
                                                .build()
                                    } else {
                                        apiResult.builder()
                                                .data(it.second.data!!
                                                        .union(apiResult.data!!).toList())
                                                .lastEvent(Result.Event.PAGINATE)
                                                .build()
                                    }
                                resultCache.replaceResultObserver.onNext(newResult)
                            })

                        }
                    }
                })
        return actionsSubject
    }

    private fun apiCallFlowable(apiRepository: ExperienceApiRepository,
                                kind: ExperienceRepoSwitch.Kind)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            ExperienceRepoSwitch.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            ExperienceRepoSwitch.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            ExperienceRepoSwitch.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable()
        }
    }

    private fun apiPaginateCallFlowable(apiRepository: ExperienceApiRepository,
                                        kind: ExperienceRepoSwitch.Kind)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            ExperienceRepoSwitch.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            ExperienceRepoSwitch.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            ExperienceRepoSwitch.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable()
        }
    }
}