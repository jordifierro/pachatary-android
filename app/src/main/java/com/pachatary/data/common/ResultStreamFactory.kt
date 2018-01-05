package com.pachatary.data.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class ResultStreamFactory<T> where T : Identifiable {

    data class ResultStream<T>(val addOrUpdateObserver: Observer<Result<List<T>>>,
                               val removeAllThatObserver: Observer<(T) -> Boolean>,
                               val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultStream<T> {
        val addOrUpdateSubject = PublishSubject.create<Result<List<T>>>()
        val removeAllThatSubject = PublishSubject.create<(T) -> Boolean>()
        val resultFlowable = Flowable.merge(
                removeAllThatSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { filterOperation: (T) -> Boolean -> Function<Result<List<T>>, Result<List<T>>>
                                { previousTResult ->
                                    val newExperiencesAfterRemove = previousTResult.data!!.filterNot(filterOperation)
                                    Result(newExperiencesAfterRemove, null)
                                } },
                addOrUpdateSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { newTListResult -> Function<Result<List<T>>, Result<List<T>>>
                                { previousTListResult ->
                                    var newList = previousTListResult.data!!
                                    for (t in newTListResult.data!!) {
                                        if (newList.find { it.id == t.id } != null)
                                            newList = newList.map { scene -> if (scene.id == t.id) t else scene }
                                        else newList = newList.union(listOf(t)).toList()
                                    }
                                    Result(newList, null)
                                }
                             }
                        )
                        .scan(Result(listOf<T>(), null), { oldValue, func -> func.apply(oldValue) })
                        .skip(1)
                        .replay(1)
                        .autoConnect()
        return ResultStream(addOrUpdateSubject, removeAllThatSubject, resultFlowable)
    }
}
