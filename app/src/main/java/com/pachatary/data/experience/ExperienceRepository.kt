package com.pachatary.data.experience

import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           val repoSwitch: ExperienceRepoSwitch) {

    fun experiencesFlowable(kind: ExperienceRepoSwitch.Kind): Flowable<Result<List<Experience>>> {
        var result = repoSwitch.getResultFlowable(kind)
        if (kind == ExperienceRepoSwitch.Kind.SAVED)
            result = result.map { it.builder().data(it.data?.filter { it.isSaved }).build() }
        return result
    }

    fun getFirstExperiences(kind: ExperienceRepoSwitch.Kind, params: Request.Params? = null) {
        repoSwitch.executeAction(kind, Request.Action.GET_FIRSTS, params)
    }

    fun getMoreExperiences(kind: ExperienceRepoSwitch.Kind) {
        repoSwitch.executeAction(kind, Request.Action.PAGINATE)
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        repoSwitch.getExperienceFlowable(experienceId)

    fun createExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.createExperience(experience).doOnNext(addOrUpdateExperienceToMine)
    }

    fun editExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.editExperience(experience).doOnNext(addOrUpdateExperienceToMine)
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String) {
        apiRepository.uploadExperiencePicture(
                experienceId, croppedImageUriString, addOrUpdateExperienceToMine)
    }

    fun saveExperience(experienceId: String, save: Boolean) {
        val disposable = experienceFlowable(experienceId)
                .map {
                    val modifier = if (save) 1 else -1
                    listOf(it.data!!.builder()
                                    .isSaved(save)
                                    .savesCount(it.data.savesCount + modifier)
                                    .build()) }
                .take(1)
                .subscribe(addOrUpdateToSavedAndUpdateToExploreAndPersonsExperiences)
        apiRepository.saveExperience(save = save, experienceId = experienceId).subscribe()
    }

    internal val addOrUpdateExperienceToMine =
        { experienceResult: Result<Experience> ->
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                list = listOf(experienceResult.data!!)) }

    private val addOrUpdateToSavedAndUpdateToExploreAndPersonsExperiences =
        { experiencesList: List<Experience> ->
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                ExperienceRepoSwitch.Modification.UPDATE_LIST, list = experiencesList)
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.PERSONS,
                    ExperienceRepoSwitch.Modification.UPDATE_LIST, list = experiencesList)
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST, list = experiencesList) }
}
