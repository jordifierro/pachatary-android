package com.pachatary.data.experience

import com.pachatary.data.common.Result
import io.reactivex.Flowable

interface ExperienceApiRepository {
    fun exploreExperiencesFlowable(word: String?, latitude: Double?, longitude: Double?)
            : Flowable<Result<List<Experience>>>

    fun myExperiencesFlowable(): Flowable<Result<List<Experience>>>
    fun savedExperiencesFlowable(): Flowable<Result<List<Experience>>>
    fun personsExperienceFlowable(username: String): Flowable<Result<List<Experience>>>
    fun paginateExperiences(url: String): Flowable<Result<List<Experience>>>
    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>>
    fun createExperience(experience: Experience): Flowable<Result<Experience>>
    fun editExperience(experience: Experience): Flowable<Result<Experience>>
    fun saveExperience(save: Boolean, experienceId: String): Flowable<Result<Void>>
    fun translateShareId(experienceShareId: String): Flowable<Result<String>>
    fun uploadExperiencePicture(experienceId: String, imageUriString: String)
                                                                    : Flowable<Result<Experience>>
}