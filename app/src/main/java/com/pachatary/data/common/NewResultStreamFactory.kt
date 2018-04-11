package com.pachatary.data.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class NewResultStreamFactory<T> where T : Identifiable {

    data class ResultStream<T>(
            val modifyResultObserver: Observer<Function<Result<List<T>>, Result<List<T>>>>,
            val addOrUpdateObserver: Observer<List<T>>,
            val updateObserver: Observer<List<T>>,
            val removeAllThatObserver: Observer<(T) -> Boolean>,
            val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultStream<T> {
        val modifyResultSubject = PublishSubject.create<Function<Result<List<T>>, Result<List<T>>>>()
        val addOrUpdateSubject = PublishSubject.create<List<T>>()
        val updateSubject = PublishSubject.create<List<T>>()
        val removeAllThatSubject = PublishSubject.create<(T) -> Boolean>()
        val resultFlowable: Flowable<Result<List<T>>> = Flowable.merge(
                modifyResultSubject.toFlowable(BackpressureStrategy.LATEST),
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
                        },
                removeAllThatSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { filterOperation: (T) -> Boolean -> Function<Result<List<T>>, Result<List<T>>>
                            { previousTResult ->
                                val newExperiencesAfterRemove = previousTResult.data!!.filterNot(filterOperation)
                                previousTResult.builder().data(newExperiencesAfterRemove).build()
                            }
                        })
        .scan(Result(listOf<T>(), lastEvent = Event.NONE),
                                { oldValue, func -> func.apply(oldValue) })
                        .replay(1)
                        .autoConnect()
        return ResultStream(modifyResultSubject, addOrUpdateSubject, updateSubject, removeAllThatSubject, resultFlowable)
    }
}
