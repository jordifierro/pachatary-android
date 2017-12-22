package com.abidria.data.common

import com.abidria.data.experience.Experience
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class ResultStreamFactory<T> where T : Identifiable {

    data class ResultStream<T>(val addListObserver: Observer<Result<List<T>>>,
                               val addOrUpdateObserver: Observer<Result<T>>,
                               val removeAllThatObserver: Observer<(T) -> Boolean>,
                               val resultFlowable: Flowable<Result<List<T>>>)

    fun create(): ResultStream<T> {
        val addListSubject = PublishSubject.create<Result<List<T>>>()
        val addOrUpdateSubject = PublishSubject.create<Result<T>>()
        val removeAllThatSubject = PublishSubject.create<(T) -> Boolean>()
        val resultFlowable = Flowable.merge(
                removeAllThatSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { filterOperation: (T) -> Boolean -> Function<Result<List<T>>, Result<List<T>>>
                                { previousTResult ->
                                    val newExperiencesAfterRemove = previousTResult.data!!.filterNot(filterOperation)
                                    Result(newExperiencesAfterRemove, null)
                                } },
                addListSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { tElementToBeAddedResult -> Function<Result<List<T>>, Result<List<T>>> {
                            previousTResult ->
                                Result(previousTResult.data!!.union(tElementToBeAddedResult.data!!).toList(),
                                        null) } },
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
        return ResultStream(addListSubject, addOrUpdateSubject, removeAllThatSubject, resultFlowable)
    }
}
