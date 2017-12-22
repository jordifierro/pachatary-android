package com.abidria.data.experience

import com.abidria.data.common.Result
import com.abidria.data.common.ResultStreamFactory
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           val experienceStreamFactory: ResultStreamFactory<Experience>) {

    private var experiencesStream: ResultStreamFactory.ResultStream<Experience>? = null
    private var exploreExperiencesFlowable: Flowable<Result<List<Experience>>>? = null
    private var myExperiencesFlowable: Flowable<Result<List<Experience>>>? = null

    private fun experiencesFlowable(): ResultStreamFactory.ResultStream<Experience> {
        if (experiencesStream == null) experiencesStream = experienceStreamFactory.create()
        return experiencesStream!!
    }

    fun exploreExperiencesFlowable() : Flowable<Result<List<Experience>>> {
        if (exploreExperiencesFlowable == null) {
            exploreExperiencesFlowable = experiencesFlowable().resultFlowable
                    .map { Result(it.data!!.filter { !it.isMine }, null) }
            refreshExperiences()
        }
        return exploreExperiencesFlowable!!
    }

    fun refreshExperiences() {
        apiRepository.exploreExperiencesFlowable().subscribe {
            experiencesStream!!.removeAllThatObserver.onNext({ experience: Experience -> !experience.isMine })
            experiencesStream!!.addOrUpdateObserver.onNext(it) }
    }

    fun myExperiencesFlowable() : Flowable<Result<List<Experience>>> {
        if (myExperiencesFlowable == null) {
            myExperiencesFlowable = experiencesFlowable().resultFlowable
                    .map { Result(it.data!!.filter { it.isMine }, null) }
            refreshMyExperiences()
        }
        return myExperiencesFlowable!!
    }

    fun refreshMyExperiences() {
        apiRepository.myExperiencesFlowable().subscribe {
            experiencesStream!!.removeAllThatObserver.onNext({ experience: Experience -> experience.isMine })
            experiencesStream!!.addOrUpdateObserver.onNext(it) }
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        experiencesFlowable().resultFlowable
                .map { Result(data = it.data?.first { it.id == experienceId }, error = it.error) }

    fun createExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.createExperience(experience).doOnNext(emitThroughAddOrUpdate)
    }

    fun editExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.editExperience(experience).doOnNext(emitThroughAddOrUpdate)
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String) {
        apiRepository.uploadExperiencePicture(experienceId, croppedImageUriString, emitThroughAddOrUpdate)
    }

    internal val emitThroughAddOrUpdate =
            { resultExperience: Result<Experience> ->
                experiencesStream!!.addOrUpdateObserver.onNext(Result(listOf(resultExperience.data!!), null)) }
}
