package com.pachatary.data.experience

import com.pachatary.data.common.Event
import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class ActionStreamFactory {

    fun create(resultStream: NewResultStreamFactory.ResultStream<Experience>,
               apiRepository: ExperienceApiRepository,
               kind: NewExperienceRepository.Kind): Observer<NewExperienceRepository.Action> {
        val actionsSubject = PublishSubject.create<NewExperienceRepository.Action>()
        actionsSubject.toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultStream.resultFlowable,
                        BiFunction<NewExperienceRepository.Action, Result<List<Experience>>,
                                Pair<NewExperienceRepository.Action, Result<List<Experience>>>>
                        { action, result -> Pair(action, result) })
                .subscribe({ if (it.first == NewExperienceRepository.Action.GET_FIRSTS) {
                    if (!it.second.isInProgress() &&
                            (it.second.hasNotBeenInitialized() || it.second.isError())) {
                        resultStream.replaceResultObserver.onNext(Result(listOf(), inProgress = true))
                        apiCallFlowable(apiRepository, kind).subscribe({ apiResult ->
                            resultStream.replaceResultObserver.onNext(
                                    apiResult.builder().lastEvent(Event.GET_FIRSTS).build())
                        })
                    }
                }
                })
        return actionsSubject
    }

    private fun apiCallFlowable(apiRepository: ExperienceApiRepository,
                                kind: NewExperienceRepository.Kind)
            : Flowable<Result<List<Experience>>> {
        when (kind) {
            NewExperienceRepository.Kind.MINE -> return apiRepository.myExperiencesFlowable()
            NewExperienceRepository.Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            NewExperienceRepository.Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable()
        }
    }
}