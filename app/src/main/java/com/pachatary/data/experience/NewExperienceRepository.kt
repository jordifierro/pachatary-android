package com.pachatary.data.experience

import com.pachatary.data.common.Event
import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Named

class NewExperienceRepository(val apiRepository: ExperienceApiRepository,
                              @Named("io") val scheduler: Scheduler,
                              resultStreamFactory: NewResultStreamFactory<Experience>) {

    enum class Kind {
        MINE, SAVED, EXPLORE
    }

    enum class Action {
        GET_FIRSTS, PAGINATE, REFRESH
    }

    private var mineResultStream = resultStreamFactory.create()
    val mineActionsSubject = PublishSubject.create<Action>()
    private var savedResultStream = resultStreamFactory.create()
    val savedActionsSubject = PublishSubject.create<Action>()
    private var exploreResultStream = resultStreamFactory.create()
    val exploreActionsSubject = PublishSubject.create<Action>()

    init {
        actionsSubject(Kind.MINE).toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultStream(Kind.MINE).resultFlowable,
                     BiFunction<Action, Result<List<Experience>>,
                                Pair<Action, Result<List<Experience>>>>
                        { action, result -> Pair(action, result) })
                .subscribe({
                    if (it.first == Action.GET_FIRSTS && it.second.hasNotBeenInitialized()) {
                        resultStream(Kind.MINE).modifyResultObserver.onNext(
                                { Result(listOf(), inProgress = true) })
                        apiCallFlowable(Kind.MINE)
                                .subscribe({ apiResult ->
                                    resultStream(Kind.MINE).modifyResultObserver.onNext(
                                        { apiResult.builder().lastEvent(Event.GET_FIRSTS).build() })
                                })
                    }
                })
    }

    fun experiencesFlowable(kind: Kind): Flowable<Result<List<Experience>>> {
        actionsSubject(kind).onNext(Action.GET_FIRSTS)
        return resultStream(kind).resultFlowable
    }

    private fun actionsSubject(kind: Kind): PublishSubject<Action> {
        when (kind) {
            Kind.MINE -> return mineActionsSubject
            Kind.SAVED -> return savedActionsSubject
            Kind.EXPLORE -> return exploreActionsSubject
        }
    }

    private fun resultStream(kind: Kind): NewResultStreamFactory.ResultStream<Experience> {
        when (kind) {
            Kind.MINE -> return mineResultStream
            Kind.SAVED -> return savedResultStream
            Kind.EXPLORE -> return exploreResultStream
        }
    }

    private fun apiCallFlowable(kind: Kind): Flowable<Result<List<Experience>>> {
        when (kind) {
            Kind.MINE -> return apiRepository.myExperiencesFlowable()
            Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable()
        }
    }
}
