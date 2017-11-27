package com.abidria.data.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class ResultStreamFactory<T> where T : Identifiable {

    data class ResultStream<T>(val replaceAllObserver: Observer<Result<List<T>>>,
                               val addOrUpdateObserver: Observer<Result<T>>,
                               val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultStream<T> {
        val replaceAllSubject = PublishSubject.create<Result<List<T>>>()
        val addOrUpdateSubject = PublishSubject.create<Result<T>>()
        val resultFlowable = Flowable.merge(
                replaceAllSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { Function<Result<List<T>>, Result<List<T>>> { _ -> it } },
                addOrUpdateSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { newTResult -> Function<Result<List<T>>, Result<List<T>>>
                                { previousTListResult ->
                                    if (previousTListResult.data!!
                                            .filter { it.id == newTResult.data!!.id }.size > 0) {
                                        val updatedTList = previousTListResult.data
                                                .map { scene ->
                                                    if (scene.id == newTResult.data!!.id) newTResult.data
                                                    else scene
                                                }
                                        Result(updatedTList.toList(), null)
                                    }
                                    else {
                                        val updatedTsSet =
                                                previousTListResult.data.union(listOf(newTResult.data!!))
                                        Result(updatedTsSet.toList(), null)
                                    }
                                }
                             }
                        )
                        .scan(Result(listOf<T>(), null), { oldValue, func -> func.apply(oldValue) })
                        .skip(1)
                        .replay(1)
                        .autoConnect()
        return ResultStream<T>(replaceAllSubject, addOrUpdateSubject, resultFlowable)
    }
}
