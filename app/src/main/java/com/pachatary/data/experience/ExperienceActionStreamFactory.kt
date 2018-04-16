package com.pachatary.data.experience

import com.pachatary.data.common.ResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class ExperienceActionStreamFactory(val apiRepository: ExperienceApiRepository) {

    enum class Action {
        GET_FIRSTS, PAGINATE, REFRESH
    }

    fun create(resultStream: ResultStreamFactory.ResultStream<Experience>,
               kind: ExperienceRepoSwitch.Kind): Observer<Action> {
        val actionsSubject = PublishSubject.create<Action>()
        val disposable = actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultStream.resultFlowable,
                        BiFunction<Action, Result<List<Experience>>,
                                Pair<Action, Result<List<Experience>>>>
                        { action, result -> Pair(action, result) })
                .subscribe({
                    if (it.first == Action.GET_FIRSTS) {
                        if (!it.second.isInProgress() &&
                                (it.second.hasNotBeenInitialized() || it.second.isError())) {
                            resultStream.replaceResultObserver.onNext(
                                    Result(listOf(), inProgress = true))
                            apiCallFlowable(apiRepository, kind).subscribe({ apiResult ->
                                resultStream.replaceResultObserver.onNext(
                                    apiResult.builder().lastEvent(Result.Event.GET_FIRSTS).build())
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
}