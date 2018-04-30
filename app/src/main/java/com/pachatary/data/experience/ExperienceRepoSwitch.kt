package com.pachatary.data.experience

import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.functions.Function3

class ExperienceRepoSwitch(resultCacheFactory: ResultCacheFactory<Experience>,
                           requesterFactory: ExperienceRequesterFactory) {

    enum class Modification {
        ADD_OR_UPDATE_LIST, UPDATE_LIST, REPLACE_RESULT
    }

    enum class Kind {
        MINE, SAVED, EXPLORE
    }

    val mineResultCache = resultCacheFactory.create()
    private val savedResultCache = resultCacheFactory.create()
    private val exploreResultCache = resultCacheFactory.create()
    private val mineActionObserver = requesterFactory.create(mineResultCache, Kind.MINE)
    private val savedActionObserver = requesterFactory.create(savedResultCache, Kind.SAVED)
    private val exploreActionObserver =
            requesterFactory.create(exploreResultCache, Kind.EXPLORE)

    fun getResultFlowable(kind: Kind) =
            when(kind) {
                Kind.MINE -> mineResultCache.resultFlowable
                Kind.SAVED -> savedResultCache.resultFlowable
                Kind.EXPLORE -> exploreResultCache.resultFlowable
            }

    fun modifyResult(kind: Kind, modification: Modification,
                     list: List<Experience>? = null, result: Result<List<Experience>>? = null) {
        when (modification) {
            Modification.ADD_OR_UPDATE_LIST -> resultCache(kind).addOrUpdateObserver.onNext(list!!)
            Modification.UPDATE_LIST -> resultCache(kind).updateObserver.onNext(list!!)
            Modification.REPLACE_RESULT -> resultCache(kind).replaceResultObserver.onNext(result!!)
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

    fun executeAction(kind: Kind, action: ExperienceRequesterFactory.Action,
                      requestParams: ExperienceRequesterFactory.RequestParams? = null) {
        when (kind) {
            Kind.MINE -> mineActionObserver.onNext(
                    ExperienceRequesterFactory.Request(action, requestParams))
            Kind.SAVED -> savedActionObserver.onNext(
                    ExperienceRequesterFactory.Request(action, requestParams))
            Kind.EXPLORE -> exploreActionObserver.onNext(
                    ExperienceRequesterFactory.Request(action, requestParams))
        }
    }

    private fun resultCache(kind: Kind) =
            when(kind) {
                Kind.MINE -> mineResultCache
                Kind.SAVED -> savedResultCache
                Kind.EXPLORE -> exploreResultCache
            }
}
