package com.pachatary.data.experience

import android.annotation.SuppressLint
import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import io.reactivex.Flowable

class ExperienceRepository(val apiRepository: ExperienceApiRepository,
                           private val repoSwitch: ExperienceRepoSwitch) {

    fun experiencesFlowable(kind: ExperienceRepoSwitch.Kind): Flowable<Result<List<Experience>>> {
        var result = repoSwitch.getResultFlowable(kind)
        if (kind == ExperienceRepoSwitch.Kind.SAVED)
            result = result.map { it.builder()
                                        .data(it.data?.filter { experience -> experience.isSaved })
                                        .build() }
        return result
    }

    fun getFirstExperiences(kind: ExperienceRepoSwitch.Kind, params: Request.Params? = null) {
        repoSwitch.executeAction(kind, Request.Action.GET_FIRSTS, params)
    }

    fun getMoreExperiences(kind: ExperienceRepoSwitch.Kind, params: Request.Params? = null) {
        repoSwitch.executeAction(kind, Request.Action.PAGINATE, params)
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
        repoSwitch.getExperienceFlowable(experienceId)
                .flatMap {
            if (it.isError() && it.error is ExperienceRepoSwitch.NotCachedExperienceException)
                    apiRepository.experienceFlowable(experienceId)
                            .doOnNext { result -> if (result.isSuccess())
                                repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                                        ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                        listOf(result.data!!)) }
            else Flowable.just(it)
                }

    @SuppressLint("CheckResult")
    fun refreshExperience(experienceId: String) {
        apiRepository.experienceFlowable(experienceId)
                .subscribe({
                    if (it.isSuccess()) {
                        repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                                ExperienceRepoSwitch.Modification.UPDATE_LIST,
                                listOf(it.data!!))
                        repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                                ExperienceRepoSwitch.Modification.UPDATE_LIST,
                                listOf(it.data))
                        repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                                ExperienceRepoSwitch.Modification.UPDATE_LIST,
                                listOf(it.data))
                        repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.PERSONS,
                                ExperienceRepoSwitch.Modification.UPDATE_LIST,
                                listOf(it.data))
                        repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                                ExperienceRepoSwitch.Modification.UPDATE_LIST,
                                listOf(it.data))
                    }
                }, { throw it })
    }

    fun createExperience(experience: Experience): Flowable<Result<Experience>> =
        apiRepository.createExperience(experience)
                .doOnNext(addOrUpdateExperienceToMine)

    fun editExperience(experience: Experience): Flowable<Result<Experience>> =
        apiRepository.editExperience(experience)
                .doOnNext(updateExperienceToMine)

    @SuppressLint("CheckResult")
    fun uploadExperiencePicture(experienceId: String, imageUriString: String) {
        apiRepository.uploadExperiencePicture(experienceId, imageUriString)
                .doOnNext(updateExperienceToMine)
                .subscribe({}, { throw it } )
    }

    @SuppressLint("CheckResult")
    fun saveExperience(experienceId: String, save: Boolean) {
        experienceFlowable(experienceId)
                .map {
                    val modifier = if (save) 1 else -1
                    listOf(it.data!!.builder()
                                    .isSaved(save)
                                    .savesCount(it.data.savesCount + modifier)
                                    .build()) }
                .take(1)
                .subscribe(addOrUpdateToSavedAndUpdateToExplorePersonsAndOtherExperiences)
                    { throw it }
        apiRepository.saveExperience(save = save, experienceId = experienceId)
                .subscribe({}, { throw it } )
    }

    fun translateShareId(experienceShareId: String): Flowable<Result<String>> =
        apiRepository.translateShareId(experienceShareId)

    fun getShareUrl(experienceId: String): Flowable<Result<String>> =
            apiRepository.getShareUrl(experienceId)

    private val addOrUpdateExperienceToMine =
        { experienceResult: Result<Experience> ->
            if (experienceResult.isSuccess())
                repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                                        ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                        list = listOf(experienceResult.data!!))
        }

    private val updateExperienceToMine =
            { experienceResult: Result<Experience> ->
                if (experienceResult.isSuccess())
                    repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.MINE,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(experienceResult.data!!))
            }

    private val addOrUpdateToSavedAndUpdateToExplorePersonsAndOtherExperiences =
        { experiencesList: List<Experience> ->
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                ExperienceRepoSwitch.Modification.UPDATE_LIST, list = experiencesList)
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.PERSONS,
                    ExperienceRepoSwitch.Modification.UPDATE_LIST, list = experiencesList)
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                    ExperienceRepoSwitch.Modification.UPDATE_LIST, list = experiencesList)
            repoSwitch.modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST, list = experiencesList) }
}
