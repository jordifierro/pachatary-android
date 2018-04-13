package com.pachatary.data.experience

import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.functions.Function3

class ExperienceRepoSwitch(apiRepository: ExperienceApiRepository,
                           resultStreamFactory: NewResultStreamFactory<Experience>,
                           actionStreamFactory: ExperienceActionStreamFactory) {

    enum class Modification {
        ADD_OR_UPDATE_LIST, UPDATE_LIST, REPLACE_RESULT
    }

    enum class Kind {
        MINE, SAVED, EXPLORE
    }

    private val mineResultStream = resultStreamFactory.create()
    private val mineActionsSubject = actionStreamFactory.create(mineResultStream,
            apiRepository, Kind.MINE)
    private val savedResultStream = resultStreamFactory.create()
    private val savedActionsSubject = actionStreamFactory.create(savedResultStream,
            apiRepository, Kind.SAVED)
    private val exploreResultStream = resultStreamFactory.create()
    private val exploreActionsSubject = actionStreamFactory.create(exploreResultStream,
            apiRepository, Kind.EXPLORE)

    fun getResultFlowable(kind: Kind) =
            when(kind) {
                Kind.MINE -> mineResultStream.resultFlowable
                Kind.SAVED -> savedResultStream.resultFlowable
                Kind.EXPLORE -> exploreResultStream.resultFlowable
            }

    fun modifyResult(kind: Kind, modification: Modification,
                     list: List<Experience>? = null, result: Result<List<Experience>>? = null) {
        when (modification) {
            Modification.ADD_OR_UPDATE_LIST -> resultStream(kind).addOrUpdateObserver.onNext(list!!)
            Modification.UPDATE_LIST -> resultStream(kind).updateObserver.onNext(list!!)
            Modification.REPLACE_RESULT -> resultStream(kind).replaceResultObserver.onNext(result!!)
        }
    }

    fun getExperienceFlowable(experienceId: String): Flowable<Result<Experience>> =
            Flowable.combineLatest(getResultFlowable(Kind.MINE),
                                   getResultFlowable(Kind.SAVED),
                                   getResultFlowable(Kind.EXPLORE),
                    Function3 { a: Result<List<Experience>>,
                                b: Result<List<Experience>>, c: Result<List<Experience>> ->
                        var datas = setOf<Experience>()
                        datas = datas.union(a.data!!)
                        datas = datas.union(b.data!!)
                        datas = datas.union(c.data!!)
                        Result(datas.toList()) })
                    .map { Result(it.data?.first { it.id == experienceId }) }

    fun executeAction(kind: Kind, action: ExperienceActionStreamFactory.Action) {
        when (kind) {
            Kind.MINE -> mineActionsSubject.onNext(action)
            Kind.SAVED -> savedActionsSubject.onNext(action)
            Kind.EXPLORE -> exploreActionsSubject.onNext(action)
        }
    }

    private fun resultStream(kind: Kind) =
            when(kind) {
                Kind.MINE -> mineResultStream
                Kind.SAVED -> savedResultStream
                Kind.EXPLORE -> exploreResultStream
            }
}
