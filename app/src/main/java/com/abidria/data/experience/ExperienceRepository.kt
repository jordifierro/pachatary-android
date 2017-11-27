package com.abidria.data.experience

import com.abidria.data.common.Result
import com.abidria.data.common.ResultStreamFactory
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           val experienceStreamFactory: ResultStreamFactory<Experience>) {

    private var experiencesStream: ResultStreamFactory.ResultStream<Experience>? = null

    fun experiencesFlowable(): Flowable<Result<List<Experience>>> {
        if (experiencesStream == null) {
            experiencesStream = experienceStreamFactory.create()
            refreshExperiences()
        }
        return experiencesStream!!.resultFlowable
    }

    fun refreshExperiences() {
        apiRepository.experiencesFlowable().subscribe { experiencesStream!!.replaceAllObserver.onNext(it) }
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        experiencesFlowable().map { Result(data = it.data?.first { it.id == experienceId }, error = it.error) }
}
