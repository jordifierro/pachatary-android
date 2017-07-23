package com.abidria.data.experience

import com.abidria.data.common.Result
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository) {

    private val experiences: Flowable<Result<List<Experience>>> = apiRepository.experiencesFlowable()
                                                                               .replay(1)
                                                                               .autoConnect()

    fun experiencesFlowable(): Flowable<Result<List<Experience>>> = experiences

    fun refreshExperiences() {
        apiRepository.refreshExperiences()
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        experiences.map { Result(data = it.data?.first { it.id == experienceId }, error = it.error) }
}
