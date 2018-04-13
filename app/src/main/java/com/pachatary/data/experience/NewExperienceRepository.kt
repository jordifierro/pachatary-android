package com.pachatary.data.experience

import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function3

class NewExperienceRepository(val apiRepository: ExperienceApiRepository,
                              resultStreamFactory: NewResultStreamFactory<Experience>,
                              actionStreamFactory: ActionStreamFactory) {

    enum class Kind {
        MINE, SAVED, EXPLORE
    }

    enum class Action {
        GET_FIRSTS, PAGINATE, REFRESH
    }

    private var mineResultStream = resultStreamFactory.create()
    val mineActionsSubject = actionStreamFactory.create(mineResultStream, apiRepository, Kind.MINE)
    private var savedResultStream = resultStreamFactory.create()
    val savedActionsSubject = actionStreamFactory.create(savedResultStream, apiRepository, Kind.SAVED)
    private var exploreResultStream = resultStreamFactory.create()
    val exploreActionsSubject = actionStreamFactory.create(exploreResultStream, apiRepository, Kind.EXPLORE)

    fun experiencesFlowable(kind: Kind): Flowable<Result<List<Experience>>> {
        var result = resultStream(kind).resultFlowable
        if (kind == Kind.SAVED)
            result = result.map { it.builder().data(it.data?.filter { it.isSaved }).build() }
        return result
    }

    fun getFirstExperiences(kind: Kind) {
        actionsSubject(kind).onNext(Action.GET_FIRSTS)
    }

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
            Flowable.combineLatest(resultStream(Kind.MINE).resultFlowable,
                                   resultStream(Kind.SAVED).resultFlowable,
                                   resultStream(Kind.EXPLORE).resultFlowable,
                    Function3 { a: Result<List<Experience>>,
                                b: Result<List<Experience>>, c: Result<List<Experience>> ->
                        var datas = setOf<Experience>()
                        datas = datas.union(a.data!!)
                        datas = datas.union(b.data!!)
                        datas = datas.union(c.data!!)
                        Result(datas.toList()) })
                    .map { Result(it.data?.first { it.id == experienceId }) }

    fun createExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.createExperience(experience)
                .doOnNext({ resultStream(Kind.MINE).addOrUpdateObserver.onNext(listOf(it.data!!)) })
    }

    fun editExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.editExperience(experience)
                .doOnNext({ resultStream(Kind.MINE).addOrUpdateObserver.onNext(listOf(it.data!!)) })
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String) {
        apiRepository.uploadExperiencePicture(experienceId, croppedImageUriString,
                { resultStream(Kind.MINE).addOrUpdateObserver.onNext(listOf(it.data!!)) })
    }

    fun saveExperience(experienceId: String, save: Boolean) {
        experienceFlowable(experienceId).map {
                    val updatedExperience = Experience(id = it.data!!.id, title = it.data.title,
                            description = it.data.description, picture = it.data.picture,
                            isMine = it.data.isMine, isSaved = save)
                    listOf(updatedExperience) }
                .take(1)
                .subscribe({ updatedExperienceList ->
                    resultStream(Kind.EXPLORE).updateObserver.onNext(updatedExperienceList)
                    resultStream(Kind.SAVED).addOrUpdateObserver.onNext(updatedExperienceList)
                })
        apiRepository.saveExperience(save = save, experienceId = experienceId).subscribe()
    }

    private fun actionsSubject(kind: Kind): Observer<Action> {
        when (kind) {
            Kind.MINE -> return mineActionsSubject
            Kind.SAVED -> return savedActionsSubject
            Kind.EXPLORE -> return exploreActionsSubject
        }
    }

    private fun resultStream(kind: Kind): NewResultStreamFactory.ResultStream<Experience> {
        when (kind) {
            Kind.MINE -> return mineResultStream
            Kind.SAVED -> return savedResultStream
            Kind.EXPLORE -> return exploreResultStream
        }
    }
}
