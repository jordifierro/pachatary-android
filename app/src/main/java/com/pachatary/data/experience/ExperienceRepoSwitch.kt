package com.pachatary.data.experience

import com.pachatary.data.common.*
import io.reactivex.Flowable
import io.reactivex.functions.Function5

class ExperienceRepoSwitch(resultCacheFactory: ResultCacheFactory<Experience>,
                           requesterFactory: ExperienceRequesterFactory) {

    data class NotCachedExperienceException(val unused: Unit = Unit) : Exception()

    enum class Modification { ADD_OR_UPDATE_LIST, UPDATE_LIST, REPLACE_RESULT }
    enum class Kind { MINE, SAVED, EXPLORE, PERSONS, OTHER }

    val mineResultCache = resultCacheFactory.create()
    private val savedResultCache = resultCacheFactory.create()
    private val exploreResultCache = resultCacheFactory.create()
    private val personsResultCache = resultCacheFactory.create()
    private val otherExperiencesResultCache = resultCacheFactory.create()
    private val mineActionObserver = requesterFactory.create(mineResultCache, Kind.MINE)
    private val savedActionObserver = requesterFactory.create(savedResultCache, Kind.SAVED)
    private val exploreActionObserver = requesterFactory.create(exploreResultCache, Kind.EXPLORE)
    private val personsActionObserver = requesterFactory.create(personsResultCache, Kind.PERSONS)

    fun getResultFlowable(kind: Kind) =
            when(kind) {
                Kind.MINE -> mineResultCache.resultFlowable
                Kind.SAVED -> savedResultCache.resultFlowable
                Kind.EXPLORE -> exploreResultCache.resultFlowable
                Kind.PERSONS -> personsResultCache.resultFlowable
                Kind.OTHER -> otherExperiencesResultCache.resultFlowable
            }

    fun modifyResult(kind: Kind, modification: Modification,
                     list: List<Experience>? = null, result: Result<List<Experience>>? = null) {
        when (modification) {
            Modification.ADD_OR_UPDATE_LIST -> resultCache(kind).addOrUpdateObserver.onNext(
                    Pair(list!!, ResultCacheFactory.AddPosition.START))
            Modification.UPDATE_LIST -> resultCache(kind).updateObserver.onNext(list!!)
            Modification.REPLACE_RESULT -> resultCache(kind).replaceResultObserver.onNext(result!!)
        }
    }

    fun getExperienceFlowable(experienceId: String): Flowable<Result<Experience>> =
                Flowable.combineLatest(getResultFlowable(Kind.MINE),
                                       getResultFlowable(Kind.SAVED),
                                       getResultFlowable(Kind.EXPLORE),
                                       getResultFlowable(Kind.PERSONS),
                                       otherExperiencesResultCache.resultFlowable,
                    Function5 { a: Result<List<Experience>>, b: Result<List<Experience>>,
                                c: Result<List<Experience>>, d: Result<List<Experience>>,
                                e: Result<List<Experience>> ->
                        var datas = setOf<Experience>()
                        if (a.data != null) datas = datas.union(a.data)
                        if (b.data != null) datas = datas.union(b.data)
                        if (c.data != null) datas = datas.union(c.data)
                        if (d.data != null) datas = datas.union(d.data)
                        if (e.data != null) datas = datas.union(e.data)
                        ResultSuccess(datas.toList()) })
                    .map { ResultSuccess(it.data?.filter { it.id == experienceId }) }
                    .map { if (it.data!!.isNotEmpty()) ResultSuccess(it.data[0])
                           else ResultError(NotCachedExperienceException()) }


    fun executeAction(kind: Kind, action: Request.Action, requestParams: Request.Params? = null) {
        when (kind) {
            Kind.MINE -> mineActionObserver.onNext(Request(action, requestParams))
            Kind.SAVED -> savedActionObserver.onNext(Request(action, requestParams))
            Kind.EXPLORE -> exploreActionObserver.onNext(Request(action, requestParams))
            Kind.PERSONS -> personsActionObserver.onNext(Request(action, requestParams))
            Kind.OTHER -> Unit
        }
    }

    private fun resultCache(kind: Kind) =
        when(kind) {
            Kind.MINE -> mineResultCache
            Kind.SAVED -> savedResultCache
            Kind.EXPLORE -> exploreResultCache
            Kind.PERSONS -> personsResultCache
            Kind.OTHER -> otherExperiencesResultCache
        }
}
