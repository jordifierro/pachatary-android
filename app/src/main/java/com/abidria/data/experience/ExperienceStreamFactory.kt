package com.abidria.data.experience

import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class ExperienceStreamFactory {

    data class ExperiencesStream(val replaceAllExperiencesObserver: Observer<Result<List<Experience>>>,
                                 val addOrUpdateExperienceObserver: Observer<Result<Experience>>,
                                 val experiencesFlowable: Flowable<Result<List<Experience>>>)

    fun create(): ExperiencesStream {
        val replaceAllExperiencesSubject = PublishSubject.create<Result<List<Experience>>>()
        val addOrUpdateExperienceSubject = PublishSubject.create<Result<Experience>>()
        val experiencesFlowable = Flowable.merge(
                replaceAllExperiencesSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { Function<Result<List<Experience>>, Result<List<Experience>>> { _ -> it } },
                addOrUpdateExperienceSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { newExperienceResult -> Function<Result<List<Experience>>, Result<List<Experience>>>
                                { previousExperienceListResult ->
                                    if (previousExperienceListResult.data!!
                                            .filter { it.id == newExperienceResult.data!!.id }.size > 0) {
                                        val updatedExperienceList = previousExperienceListResult.data
                                                .map { experience ->
                                                    if (experience.id == newExperienceResult.data!!.id)
                                                        newExperienceResult.data
                                                    else experience
                                                }
                                        Result(updatedExperienceList.toList(), null)
                                    }
                                    else {
                                        val updatedExperiencesSet =
                                                previousExperienceListResult.data.union(listOf(newExperienceResult.data!!))
                                        Result(updatedExperiencesSet.toList(), null)
                                    }
                                }
                             }
                        )
                        .scan(Result(listOf<Experience>(), null), { oldValue, func -> func.apply(oldValue) })
                        .skip(1)
                        .replay(1)
                        .autoConnect()
        return ExperiencesStream(replaceAllExperiencesSubject, addOrUpdateExperienceSubject, experiencesFlowable)
    }
}
