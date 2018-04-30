package com.pachatary.data.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class ResultCacheFactory<T> where T : Identifiable {

    data class ResultCache<T>(
            val replaceResultObserver: Observer<Result<List<T>>>,
            val addOrUpdateObserver: Observer<List<T>>,
            val updateObserver: Observer<List<T>>,
            val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultCache<T> {
        val replaceResultSubject = PublishSubject.create<Result<List<T>>>()
        val addOrUpdateSubject = PublishSubject.create<List<T>>()
        val updateSubject = PublishSubject.create<List<T>>()
        val resultFlowable =
            Flowable.merge(
                replaceResultSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { newResult: Result<List<T>> ->
                            Function<Result<List<T>>, Result<List<T>>> { newResult }},
                addOrUpdateSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { tList: List<T> -> Function<Result<List<T>>, Result<List<T>>>
                            { previousTListResult: Result<List<T>> ->
                                var newList = previousTListResult.data!!
                                for (t in tList) {
                                    if (newList.find { it.id == t.id } != null)
                                        newList = newList.map { x -> if (x.id == t.id) t else x }
                                    else newList = newList.union(listOf(t)).toList()
                                }
                                previousTListResult.builder().data(newList).build()
                            }
                        },
                updateSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { tList: List<T> -> Function<Result<List<T>>, Result<List<T>>>
                            { previousTListResult: Result<List<T>> ->
                                var newList = previousTListResult.data!!
                                for (t in tList) {
                                    newList = newList.map { x -> if (x.id == t.id) t else x }
                                }
                                previousTListResult.builder().data(newList).build()
                            }
                        })
                    .scan(Result(listOf<T>(), action = Request.Action.NONE),
                            { oldValue, func -> func.apply(oldValue) })
                    .replay(1)
                    .autoConnect()
        return ResultCache(replaceResultSubject, addOrUpdateSubject, updateSubject, resultFlowable)
    }
}
