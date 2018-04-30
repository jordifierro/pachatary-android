package com.pachatary.data.experience

import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultCacheFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class ExperienceRequesterFactory(val apiRepository: ExperienceApiRepository) {

    fun create(resultCache: ResultCacheFactory.ResultCache<Experience>,
               kind: ExperienceRepoSwitch.Kind): Observer<Request> {
        val actionsSubject = PublishSubject.create<Request>()
        val disposable = actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultCache.resultFlowable,
                        BiFunction<Request, Result<List<Experience>>,
                                Pair<Request, Result<List<Experience>>>>
                        { request, result -> Pair(request, result) })
                .subscribe({
                    if (it.first.action == Request.Action.GET_FIRSTS) {
                        if ((!it.second.isInProgress() &&
                                (!it.second.hasBeenInitialized() || it.second.isError()))
                        || it.first.params != it.second.params) {
                            resultCache.replaceResultObserver.onNext(Result(listOf(),
                                    inProgress = true,
                                    action = Request.Action.GET_FIRSTS,
                                    params = it.first.params))
                            apiCallFlowable(apiRepository, kind, it.first.params)
                                    .subscribe({ apiResult ->
                                resultCache.replaceResultObserver.onNext(
                                    apiResult.builder()
                                            .action(Request.Action.GET_FIRSTS)
                                            .params(it.first.params)
                                            .build())
                            })
                        }
                    }
                    else if (it.first.action == Request.Action.PAGINATE) {
                        if (!it.second.isInProgress() &&
                                (it.second.isSuccess() && it.second.hasBeenInitialized() ||
                            it.second.isError() && it.second.action == Request.Action.PAGINATE) &&
                                it.second.hasMoreElements()) {
                            resultCache.replaceResultObserver.onNext(
                                    it.second.builder()
                                                .inProgress(true)
                                                .action(Request.Action.PAGINATE)
                                                .error(null)
                                            .build())
                            apiRepository.paginateExperiences(it.second.nextUrl!!).subscribe(
                            { apiResult ->
                                val newResult =
                                    if (apiResult.isError()) {
                                        it.second.builder()
                                                .action(Request.Action.PAGINATE)
                                                .error(apiResult.error)
                                                .build()
                                    } else {
                                        apiResult.builder()
                                                .data(it.second.data!!
                                                        .union(apiResult.data!!).toList())
                                                .action(Request.Action.PAGINATE)
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
                                kind: ExperienceRepoSwitch.Kind, requestParams: Request.Params?)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            ExperienceRepoSwitch.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            ExperienceRepoSwitch.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            ExperienceRepoSwitch.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable(
                    requestParams!!.word, requestParams.latitude, requestParams.longintude)
        }
    }
}