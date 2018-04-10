package com.pachatary.data.experience

import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultStreamFactory
import io.reactivex.Flowable
import io.reactivex.Scheduler
import javax.inject.Named

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           @Named("io") val scheduler: Scheduler,
                           val experienceStreamFactory: ResultStreamFactory<Experience>) {

    private var experiencesStream: ResultStreamFactory.ResultStream<Experience>? = null
    private var exploreExperiencesFlowable: Flowable<Result<List<Experience>>>? = null
    private var myExperiencesFlowable: Flowable<Result<List<Experience>>>? = null
    private var savedExperiencesFlowable: Flowable<Result<List<Experience>>>? = null

    private fun experiencesFlowable(): ResultStreamFactory.ResultStream<Experience> {
        if (experiencesStream == null) experiencesStream = experienceStreamFactory.create()
        return experiencesStream!!
    }

    fun exploreExperiencesFlowable() : Flowable<Result<List<Experience>>> {
        if (exploreExperiencesFlowable == null) {
            exploreExperiencesFlowable = experiencesFlowable().resultFlowable
                    .map { Result(it.data!!.filter { !it.isMine && !it.isSaved }) }
            refreshExperiences()
        }
        return exploreExperiencesFlowable!!
    }

    fun refreshExperiences() {
        apiRepository.exploreExperiencesFlowable().subscribe {
            experiencesStream!!.removeAllThatObserver.onNext({ !it.isMine && !it.isSaved })
            experiencesStream!!.addOrUpdateObserver.onNext(it) }
    }

    fun myExperiencesFlowable() : Flowable<Result<List<Experience>>> {
        if (myExperiencesFlowable == null) {
            myExperiencesFlowable = experiencesFlowable().resultFlowable
                    .map { Result(it.data!!.filter { it.isMine }) }
            refreshMyExperiences()
        }
        return myExperiencesFlowable!!
    }

    fun refreshMyExperiences() {
        apiRepository.myExperiencesFlowable().subscribe {
            experiencesStream!!.removeAllThatObserver.onNext({ experience: Experience -> experience.isMine })
            experiencesStream!!.addOrUpdateObserver.onNext(it) }
    }

    fun savedExperiencesFlowable() : Flowable<Result<List<Experience>>> {
        if (savedExperiencesFlowable == null) {
            savedExperiencesFlowable = experiencesFlowable().resultFlowable
                    .map { Result(it.data!!.filter { it.isSaved }) }
            refreshSavedExperiences()
        }
        return savedExperiencesFlowable!!
    }

    fun refreshSavedExperiences() {
        apiRepository.savedExperiencesFlowable().subscribe {
            experiencesStream!!.removeAllThatObserver.onNext(
                    { experience: Experience -> experience.isSaved })
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
                experiencesStream!!.addOrUpdateObserver.onNext(Result(listOf(resultExperience.data!!))) }

    fun saveExperience(experienceId: String, save: Boolean) {
        experienceFlowable(experienceId).map {
            val updatedExperience = Experience(id = it.data!!.id, title = it.data.title,
                    description = it.data.description, picture = it.data.picture,
                    isMine = it.data.isMine, isSaved = save)
            Result(listOf(updatedExperience)) }
                                        .subscribeOn(scheduler)
                                        .take(1)
                                        .subscribe({ experiencesStream!!.addOrUpdateObserver.onNext(it) })
        apiRepository.saveExperience(save = save, experienceId = experienceId).subscribeOn(scheduler).subscribe()
    }
}
