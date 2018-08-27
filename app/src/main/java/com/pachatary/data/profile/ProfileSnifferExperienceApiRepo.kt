package com.pachatary.data.profile

import com.pachatary.data.common.Result
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceApiRepository
import io.reactivex.Flowable

class ProfileSnifferExperienceApiRepo(private val profileRepo: ProfileRepository,
                                      private val experienceApiRepo: ExperienceApiRepository)
                                                                        : ExperienceApiRepository {

    override fun exploreExperiencesFlowable(word: String?, latitude: Double?, longitude: Double?) =
            experienceApiRepo.exploreExperiencesFlowable(word, latitude, longitude)
                    .doOnNext(sniffProfiles)!!
    override fun myExperiencesFlowable() =
            experienceApiRepo.myExperiencesFlowable()
                    .doOnNext(sniffProfiles)!!
    override fun savedExperiencesFlowable() =
            experienceApiRepo.savedExperiencesFlowable()
                    .doOnNext(sniffProfiles)!!
    override fun personsExperienceFlowable(username: String) =
            experienceApiRepo.personsExperienceFlowable(username)
    override fun paginateExperiences(url: String) =
            experienceApiRepo.paginateExperiences(url)
                    .doOnNext(sniffProfiles)!!
    override fun experienceFlowable(experienceId: String) =
            experienceApiRepo.experienceFlowable(experienceId)
                    .doOnNext(sniffProfile)!!
    override fun createExperience(experience: Experience) =
            experienceApiRepo.createExperience(experience)
    override fun editExperience(experience: Experience) =
            experienceApiRepo.editExperience(experience)
    override fun saveExperience(save: Boolean, experienceId: String) =
            experienceApiRepo.saveExperience(save, experienceId)
    override fun translateShareId(experienceShareId: String) =
            experienceApiRepo.translateShareId(experienceShareId)
    override fun getShareUrl(experienceId: String): Flowable<Result<String>> =
            experienceApiRepo.getShareUrl(experienceId)
    override fun uploadExperiencePicture(experienceId: String, imageUriString: String) =
            experienceApiRepo.uploadExperiencePicture(experienceId, imageUriString)

    private val sniffProfiles = { result: Result<List<Experience>> ->
        if (result.isSuccess())
            for (experience in result.data!!)
                profileRepo.cacheProfile(experience.authorProfile)
    }

    private val sniffProfile = { result: Result<Experience> ->
        if (result.isSuccess()) profileRepo.cacheProfile(result.data!!.authorProfile)
    }
}
