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

    data class RequestParams(val word: String? = null,
                             val latitude: Double? = null,
                             val longintude: Double? = null) {}

    data class Request(val action: Action, val params: RequestParams? = null) {}

    fun create(resultCache: ResultCacheFactory.ResultCache<Experience>,
               kind: ExperienceRepoSwitch.Kind): Observer<Request> {
        val actionsSubject = PublishSubject.create<Request>()
        val disposable = actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultCache.resultFlowable,
                        BiFunction<Request, Result<List<Experience>>,
                                Pair<Request, Result<List<Experience>>>>
                        { request, result -> Pair(request, result) })
                .subscribe({
                    if (it.first.action == Action.GET_FIRSTS) {
                        if (!it.second.isInProgress() &&
                                (!it.second.hasBeenInitialized() || it.second.isError())) {
                            resultCache.replaceResultObserver.onNext(Result(listOf(),
                                    inProgress = true, lastEvent = Result.Event.GET_FIRSTS))
                            apiCallFlowable(apiRepository, kind, it.first.params)
                                    .subscribe({ apiResult ->
                                resultCache.replaceResultObserver.onNext(
                                    apiResult.builder().lastEvent(Result.Event.GET_FIRSTS).build())
                            })
                        }
                    }
                    else if (it.first.action == Action.PAGINATE) {
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
                                kind: ExperienceRepoSwitch.Kind, requestParams: RequestParams?)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            ExperienceRepoSwitch.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            ExperienceRepoSwitch.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            ExperienceRepoSwitch.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable(
                    requestParams!!.word, requestParams.latitude, requestParams.longintude)
        }
    }
}