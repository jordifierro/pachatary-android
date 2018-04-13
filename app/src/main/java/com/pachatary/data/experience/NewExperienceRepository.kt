package com.pachatary.data.experience

import com.pachatary.data.common.Result
import io.reactivex.Flowable

class NewExperienceRepository(val apiRepository: ExperienceApiRepository,
                              val repoSwitch: ExperienceRepoSwitch) {

    fun experiencesFlowable(kind: ExperienceRepoSwitch.Kind): Flowable<Result<List<Experience>>> {
        var result = repoSwitch.getResultFlowable(kind)
        if (kind == ExperienceRepoSwitch.Kind.SAVED)
            result = result.map { it.builder().data(it.data?.filter { it.isSaved }).build() }
        return result
    }

    fun getFirstExperiences(kind: ExperienceRepoSwitch.Kind) {
        repoSwitch.executeAction(kind, ExperienceActionStreamFactory.Action.GET_FIRSTS)
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        repoSwitch.getExperienceFlowable(experienceId)

    fun createExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.createExperience(experience)
                .doOnNext({ repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                                ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                list = listOf(it.data!!)) })
    }

    fun editExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.editExperience(experience)
                .doOnNext({ repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                                ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                list = listOf(it.data!!)) })
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String) {
        apiRepository.uploadExperiencePicture(experienceId, croppedImageUriString,
                { repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                        ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                        list = listOf(it.data!!)) })
    }

    fun saveExperience(experienceId: String, save: Boolean) {
        val disposable = experienceFlowable(experienceId).map {
            val updatedExperience = Experience(id = it.data!!.id, title = it.data.title,
                    description = it.data.description, picture = it.data.picture,
                    isMine = it.data.isMine, isSaved = save)
            listOf(updatedExperience)
        }
                .take(1)
                .subscribe({ updatedExperienceList ->
                    repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = updatedExperienceList)
                    repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                            ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                            list = updatedExperienceList)
                })
        apiRepository.saveExperience(save = save, experienceId = experienceId).subscribe()
    }
}
