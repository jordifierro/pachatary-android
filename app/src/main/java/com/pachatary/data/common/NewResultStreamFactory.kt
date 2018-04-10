package com.pachatary.data.common

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class NewResultStreamFactory<T> where T : Identifiable {

    data class ResultStream<T>(
            val modifyResultObserver: Observer<(Result<List<T>>) -> Result<List<T>>>,
            val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultStream<T> {
        val modifyResultObserver = PublishSubject.create<(Result<List<T>>) -> Result<List<T>>>()
        val resultFlowable = modifyResultObserver
                        .scan(Result(listOf<T>(), lastEvent = Event.NONE),
                              { oldValue, func -> func(oldValue) })
                        .replay(1)
                        .autoConnect()
                        .toFlowable(BackpressureStrategy.LATEST)
        return ResultStream(modifyResultObserver, resultFlowable)
    }
}
