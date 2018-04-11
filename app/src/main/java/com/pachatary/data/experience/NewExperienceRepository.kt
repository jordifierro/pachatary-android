package com.pachatary.data.experience

import com.pachatary.data.common.Event
import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import java.util.*
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
                            resultStream(kind).modifyResultObserver.onNext(
                                    { Result(listOf(), inProgress = true) })
                            apiCallFlowable(kind)
                                .subscribe({ apiResult ->
                                    resultStream(kind).modifyResultObserver.onNext(
                                        { apiResult.builder().lastEvent(Event.GET_FIRSTS).build() })
                                })
                        }
                    }
                    })
        }
    }

    fun experiencesFlowable(kind: Kind): Flowable<Result<List<Experience>>> {
        return resultStream(kind).resultFlowable
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
    }

    fun editExperience(experience: Experience): Flowable<Result<Experience>> {
        return apiRepository.editExperience(experience)
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String) {
        apiRepository.uploadExperiencePicture(experienceId, croppedImageUriString, {})
    }

    fun saveExperience(experienceId: String, save: Boolean) {
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
