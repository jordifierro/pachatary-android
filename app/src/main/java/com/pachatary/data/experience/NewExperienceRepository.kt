package com.pachatary.data.experience

import com.pachatary.data.common.Event
import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import javax.inject.Named

class NewExperienceRepository(val apiRepository: ExperienceApiRepository,
                              @Named("io") val scheduler: Scheduler,
                              resultStreamFactory: NewResultStreamFactory<Experience>) {

    enum class Kind {
        MINE, SAVED, EXPLORE
    }

    enum class Action {
        GET_FIRSTS, PAGINATE, REFRESH
    }

    private var mineResultStream = resultStreamFactory.create()
    val mineActionsSubject = PublishSubject.create<Action>()
    private var savedResultStream = resultStreamFactory.create()
    val savedActionsSubject = PublishSubject.create<Action>()
    private var exploreResultStream = resultStreamFactory.create()
    val exploreActionsSubject = PublishSubject.create<Action>()

    init {
        for (kind in Kind.values()) {
            actionsSubject(kind).toFlowable(BackpressureStrategy.LATEST)
                .withLatestFrom(resultStream(kind).resultFlowable,
                        BiFunction<Action, Result<List<Experience>>,
                                Pair<Action, Result<List<Experience>>>>
                        { action, result -> Pair(action, result) })
                .subscribe({ if (it.first == Action.GET_FIRSTS) {
                    if (!it.second.isInProgress() &&
                            (it.second.hasNotBeenInitialized() || it.second.isError())) {
                        resultStream(kind).replaceResultObserver.onNext(
                                Result(listOf(), inProgress = true))
                        apiCallFlowable(kind).subscribe({ apiResult ->
                                resultStream(kind).replaceResultObserver.onNext(
                                    apiResult.builder().lastEvent(Event.GET_FIRSTS).build())
                            })
                    }
                }
                })
        }
    }

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
        val disposable = experienceFlowable(experienceId).map {
                    val updatedExperience = Experience(id = it.data!!.id, title = it.data.title,
                            description = it.data.description, picture = it.data.picture,
                            isMine = it.data.isMine, isSaved = save)
                    listOf(updatedExperience) }
                .subscribeOn(scheduler)
                .take(1)
                .subscribe({ updatedExperienceList ->
                    resultStream(Kind.EXPLORE).updateObserver.onNext(updatedExperienceList)
                    resultStream(Kind.SAVED).addOrUpdateObserver.onNext(updatedExperienceList)
                })
        apiRepository.saveExperience(save = save, experienceId = experienceId).subscribe()
    }

    private fun actionsSubject(kind: Kind): PublishSubject<Action> {
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

    private fun apiCallFlowable(kind: Kind): Flowable<Result<List<Experience>>> {
        when (kind) {
            Kind.MINE -> return apiRepository.myExperiencesFlowable()
            Kind.SAVED -> return apiRepository.savedExperiencesFlowable()
            Kind.EXPLORE -> return apiRepository.exploreExperiencesFlowable()
        }
    }
}
