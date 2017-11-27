package com.abidria.data.experience

import com.abidria.data.common.Result
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           val experienceStreamFactory: ExperienceStreamFactory) {

    private var experiencesStream: ExperienceStreamFactory.ExperiencesStream? = null

    fun experiencesFlowable(): Flowable<Result<List<Experience>>> {
        if (experiencesStream == null) {
            experiencesStream = experienceStreamFactory.create()
            refreshExperiences()
        }
        return experiencesStream!!.experiencesFlowable
    }

    fun refreshExperiences() {
        apiRepository.experiencesFlowable().subscribe { experiencesStream!!.replaceAllExperiencesObserver.onNext(it) }
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        experiencesFlowable().map { Result(data = it.data?.first { it.id == experienceId }, error = it.error) }
}
