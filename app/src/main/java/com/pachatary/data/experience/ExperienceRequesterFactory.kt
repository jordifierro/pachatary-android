package com.pachatary.data.experience

import android.annotation.SuppressLint
import com.pachatary.data.common.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class ExperienceRequesterFactory(val apiRepository: ExperienceApiRepository) {

    @SuppressLint("CheckResult")
    fun create(resultCache: ResultCacheFactory.ResultCache<Experience>,
               kind: ExperienceRepoSwitch.Kind): Observer<Request> {
        val actionsSubject = PublishSubject.create<Request>()
        actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultCache.resultFlowable,
                        BiFunction<Request, Result<List<Experience>>,
                                Pair<Request, Result<List<Experience>>>>
                        { request, result -> Pair(request, result) })
                .subscribe({
                    if (it.first.action == Request.Action.GET_FIRSTS) {
                        if (!it.second.isInProgress()
                        || it.first.params != it.second.params) {
                            resultCache.replaceResultObserver.onNext(
                                    ResultInProgress(listOf(), action = Request.Action.GET_FIRSTS,
                                                     params = it.first.params))
                            apiCallFlowable(apiRepository, kind, it.first.params)
                                    .subscribe({ apiResult ->
                                resultCache.replaceResultObserver.onNext(
                                    apiResult.builder()
                                            .action(Request.Action.GET_FIRSTS)
                                            .params(it.first.params)
                                            .build())
                                    }, { throw it })
                        }
                    }
                    else if (it.first.action == Request.Action.PAGINATE &&
                             it.first.params != it.second.params) {
                        actionsSubject.onNext(Request(Request.Action.GET_FIRSTS, it.first.params))
                    }
                    else if (it.first.action == Request.Action.PAGINATE) {
                        if (!it.second.isInProgress() &&
                                (it.second.isSuccess() && it.second.hasBeenInitialized() ||
                            it.second.isError() && it.second.action == Request.Action.PAGINATE) &&
                                it.second.hasMoreElements()) {
                            resultCache.replaceResultObserver.onNext(
                                    it.second.builder()
                                                .status(Status.IN_PROGRESS)
                                                .action(Request.Action.PAGINATE)
                                                .error(null)
                                            .build())
                            apiRepository.paginateExperiences(it.second.nextUrl!!).subscribe(
                            { apiResult ->
                                val newResult =
                                    if (apiResult.isError()) {
                                        it.second.builder()
                                                .status(Status.ERROR)
                                                .error(apiResult.error)
                                                .action(Request.Action.PAGINATE)
                                                .build()
                                    } else {
                                        apiResult.builder()
                                                .data(it.second.data!!
                                                        .union(apiResult.data!!).toList())
                                                .action(Request.Action.PAGINATE)
                                                .params(it.second.params)
                                                .build()
                                    }
                                resultCache.replaceResultObserver.onNext(newResult)
                            }, { throw it })
                        }
                    }
                }, { throw it })
        return actionsSubject
    }

    private fun apiCallFlowable(apiRepository: ExperienceApiRepository,
                                kind: ExperienceRepoSwitch.Kind, requestParams: Request.Params?)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            ExperienceRepoSwitch.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            ExperienceRepoSwitch.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            ExperienceRepoSwitch.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable(
                    requestParams!!.word, requestParams.latitude, requestParams.longitude)
            ExperienceRepoSwitch.Kind.PERSONS ->
                return apiRepository.personsExperienceFlowable(requestParams!!.username!!)
            ExperienceRepoSwitch.Kind.OTHER -> return Flowable.empty()
        }
    }
}