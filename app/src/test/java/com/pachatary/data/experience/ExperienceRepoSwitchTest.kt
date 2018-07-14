package com.pachatary.data.experience

import com.pachatary.data.common.*
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceRepoSwitchTest {

    @Test
    fun test_get_result_flowable_returns_kind_result_flowable() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
            } whenn {
                initialize_switch()
                get_result_flowable()
            } then {
                result_should_be_kind_result_flowable()
            }
        }
    }

    @Test
    fun test_modify_result_with_add_or_update_list() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_list_of_experiences()
                a_modification(ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST)
            } whenn {
                initialize_switch()
                modify_result()
            } then {
                should_emit_that_list_through_kind_add_or_update()
            }
        }
    }

    @Test
    fun test_modify_result_with_update_list() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_list_of_experiences()
                a_modification(ExperienceRepoSwitch.Modification.UPDATE_LIST)
            } whenn {
                initialize_switch()
                modify_result()
            } then {
                should_emit_that_list_through_kind_update()
            }
        }
    }

    @Test
    fun test_modify_result_with_replace_result() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_list_of_experiences()
                a_result_with_that_list()
                a_modification(ExperienceRepoSwitch.Modification.REPLACE_RESULT)
            } whenn {
                initialize_switch()
                modify_result()
            } then {
                should_emit_that_result_through_replace_result()
            }
        }
    }

    @Test
    fun test_get_experience_flowable_joins_five_caches_and_filters_experience_by_id() {
        given {
            an_experience_id()
            an_experience_with_that_id()
            some_result_flowables_that_emit_different_and_repeated_experiences()
        } whenn {
            initialize_switch()
            get_experience_flowable_for_that_id()
        } then {
            should_return_a_flowable_with_experience_only_once()
        }
    }

    @Test
    fun test_get_experience_flowable_when_experience_not_cached_emits_not_cache_error() {
        given {
            an_experience_id()
            an_experience_with_that_id()
            some_result_flowables_that_dont_emit_the_experience()
        } whenn {
            initialize_switch()
            get_experience_flowable_for_that_id()
        } then {
            should_return_result_with_not_cached_error()
        }
    }
    @Test
    fun test_execute_action_emits_through_appropiate_observer() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            for(action in Request.Action.values()) {
                given {
                    a_kind(kind)
                    an_action(action)
                    a_request_params()
                } whenn {
                    initialize_switch()
                    execute_action()
                } then {
                    should_emit_that_action_through_kind_action_observer()
                }
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        @Mock lateinit var resultCacheFactory: ResultCacheFactory<Experience>
        @Mock lateinit var requesterFactory: ExperienceRequesterFactory
        lateinit var switch: ExperienceRepoSwitch

        val addOrUpdateMineObserver = TestObserver.create<Pair<List<Experience>,
                                                               ResultCacheFactory.AddPosition>>()
        val updateMineObserver = TestObserver.create<List<Experience>>()
        val replaceResultMineObserver = TestObserver.create<Result<List<Experience>>>()
        var resultMineFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultMineCache = ResultCacheFactory.ResultCache(replaceResultMineObserver,
                addOrUpdateMineObserver, updateMineObserver, resultMineFlowable)

        val addOrUpdateSavedObserver = TestObserver.create<Pair<List<Experience>,
                                                                ResultCacheFactory.AddPosition>>()
        val updateSavedObserver = TestObserver.create<List<Experience>>()
        val replaceResultSavedObserver = TestObserver.create<Result<List<Experience>>>()
        var resultSavedFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultSavedCache = ResultCacheFactory.ResultCache(replaceResultSavedObserver,
                addOrUpdateSavedObserver, updateSavedObserver, resultSavedFlowable)

        val addOrUpdateExploreObserver = TestObserver.create<Pair<List<Experience>,
                                                                  ResultCacheFactory.AddPosition>>()
        val updateExploreObserver = TestObserver.create<List<Experience>>()
        val replaceResultExploreObserver = TestObserver.create<Result<List<Experience>>>()
        var resultExploreFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultExploreCache = ResultCacheFactory.ResultCache(replaceResultExploreObserver,
                addOrUpdateExploreObserver, updateExploreObserver, resultExploreFlowable)

        val addOrUpdatePersonsObserver = TestObserver.create<Pair<List<Experience>,
                                                                  ResultCacheFactory.AddPosition>>()
        val updatePersonsObserver = TestObserver.create<List<Experience>>()
        val replaceResultPersonsObserver = TestObserver.create<Result<List<Experience>>>()
        var resultPersonsFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultPersonsCache = ResultCacheFactory.ResultCache(replaceResultPersonsObserver,
                addOrUpdatePersonsObserver, updatePersonsObserver, resultPersonsFlowable)

        val addOrUpdateOtherObserver = TestObserver.create<Pair<List<Experience>,
                                                                ResultCacheFactory.AddPosition>>()
        val updateOtherObserver = TestObserver.create<List<Experience>>()
        val replaceResultOtherObserver = TestObserver.create<Result<List<Experience>>>()
        var resultOtherFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultOtherCache = ResultCacheFactory.ResultCache(replaceResultOtherObserver,
                addOrUpdateOtherObserver, updateOtherObserver, resultOtherFlowable)

        val testMineActionObserver = TestObserver.create<Request>()
        val testSavedActionObserver = TestObserver.create<Request>()
        val testExploreActionObserver = TestObserver.create<Request>()
        val testPersonsActionObserver = TestObserver.create<Request>()

        var kind = ExperienceRepoSwitch.Kind.MINE
        var action = Request.Action.GET_FIRSTS
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        lateinit var experienceList: List<Experience>
        var experiencesResult: Result<List<Experience>>? = null
        var modification = ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST
        var experienceId = ""
        lateinit var experience: Experience
        lateinit var resultExperienceFlowable: Flowable<Result<Experience>>
        lateinit var requestParams: Request.Params

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            return this
        }

        fun initialize_switch() {
            resultMineCache = ResultCacheFactory.ResultCache(replaceResultMineObserver,
                    addOrUpdateMineObserver, updateMineObserver, resultMineFlowable)
            resultSavedCache = ResultCacheFactory.ResultCache(replaceResultSavedObserver,
                    addOrUpdateSavedObserver, updateSavedObserver, resultSavedFlowable)
            resultExploreCache = ResultCacheFactory.ResultCache(replaceResultExploreObserver,
                    addOrUpdateExploreObserver, updateExploreObserver, resultExploreFlowable)
            resultPersonsCache = ResultCacheFactory.ResultCache(replaceResultPersonsObserver,
                    addOrUpdatePersonsObserver, updatePersonsObserver, resultPersonsFlowable)
            resultOtherCache = ResultCacheFactory.ResultCache(replaceResultOtherObserver,
                    addOrUpdateOtherObserver, updateOtherObserver, resultOtherFlowable)
            BDDMockito.given(resultCacheFactory.create())
                    .willReturn(resultMineCache)
                    .willReturn(resultSavedCache)
                    .willReturn(resultExploreCache)
                    .willReturn(resultPersonsCache)
                    .willReturn(resultOtherCache)
            BDDMockito.given(
                    requesterFactory.create(resultMineCache, ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(testMineActionObserver)
            BDDMockito.given(
                    requesterFactory.create(resultSavedCache, ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(testSavedActionObserver)
            BDDMockito.given(requesterFactory.create(resultExploreCache,
                    ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(testExploreActionObserver)
            BDDMockito.given(requesterFactory.create(resultPersonsCache,
                    ExperienceRepoSwitch.Kind.PERSONS))
                    .willReturn(testPersonsActionObserver)

            switch = ExperienceRepoSwitch(resultCacheFactory, requesterFactory)
        }

        fun an_experience_id() {
            experienceId = "8"
        }

        fun an_experience_with_that_id() {
            experience = Experience(experienceId, "t", "", null, true, true)
        }

        fun a_request_params() {
            requestParams = Request.Params("c", 8.0, -9.1, "usr.nm")
        }

        fun some_result_flowables_that_emit_different_and_repeated_experiences() {
            resultMineFlowable = Flowable.just(ResultSuccess(listOf(
                    experience,
                    Experience("1", "", "", null, true, false))))
            resultSavedFlowable = Flowable.just(ResultSuccess(listOf(
                    experience,
                    Experience("4", "", "", null, true, false))))
            resultExploreFlowable = Flowable.just(ResultSuccess(listOf(
                    Experience("4", "", "", null, true, false),
                    Experience("4", "", "", null, true, false))))
            resultPersonsFlowable = Flowable.just(ResultError(Exception()))
            resultOtherFlowable = Flowable.just(ResultInProgress())
        }

        fun some_result_flowables_that_dont_emit_the_experience() {
            resultMineFlowable = Flowable.just(ResultSuccess(listOf(
                    Experience("1", "", "", null, true, false))))
            resultSavedFlowable = Flowable.just(ResultSuccess(listOf(
                    Experience("4", "", "", null, true, false))))
            resultExploreFlowable = Flowable.just(ResultSuccess(listOf(
                    Experience("4", "", "", null, true, false),
                    Experience("3", "", "", null, true, false))))
            resultPersonsFlowable = Flowable.just(ResultSuccess(listOf(
                    Experience("6", "", "", null, true, false),
                    Experience("4", "", "", null, true, false))))
            resultOtherFlowable = Flowable.just(ResultSuccess(listOf()))
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun an_action(action: Request.Action) {
            this.action = action
        }

        fun a_list_of_experiences() {
            experienceList = listOf(Experience("1", "t", "d", null, false, false),
                                    Experience("2", "t", "d", null, true, false))
        }

        fun a_result_with_that_list() {
            experiencesResult = ResultSuccess(experienceList)
        }

        fun a_modification(modification: ExperienceRepoSwitch.Modification) {
            this.modification = modification
        }

        fun get_result_flowable() {
            resultFlowable = switch.getResultFlowable(kind)
        }

        fun get_experience_flowable_for_that_id() {
            resultExperienceFlowable = switch.getExperienceFlowable(experienceId)
        }

        fun modify_result() {
            switch.modifyResult(kind, modification, experienceList, experiencesResult)
        }

        fun execute_action() {
            switch.executeAction(kind, action, requestParams)
        }

        fun result_should_be_kind_result_flowable() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> assertEquals(resultMineFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.SAVED -> assertEquals(resultSavedFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.EXPLORE ->
                    assertEquals(resultExploreFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.PERSONS ->
                    assertEquals(resultPersonsFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.OTHER -> assertEquals(resultOtherFlowable, resultFlowable)
            }
        }

        fun should_emit_that_list_through_kind_add_or_update() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    addOrUpdateMineObserver.awaitCount(1)
                    addOrUpdateMineObserver.assertValue(
                            Pair(experienceList, ResultCacheFactory.AddPosition.START))
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    addOrUpdateSavedObserver.awaitCount(1)
                    addOrUpdateSavedObserver.assertValue(
                            Pair(experienceList, ResultCacheFactory.AddPosition.START))
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    addOrUpdateExploreObserver.awaitCount(1)
                    addOrUpdateExploreObserver.assertValue(
                            Pair(experienceList, ResultCacheFactory.AddPosition.START))
                }
                ExperienceRepoSwitch.Kind.PERSONS -> {
                    addOrUpdatePersonsObserver.awaitCount(1)
                    addOrUpdatePersonsObserver.assertValue(
                            Pair(experienceList, ResultCacheFactory.AddPosition.START))
                }
                ExperienceRepoSwitch.Kind.OTHER -> {
                    addOrUpdateOtherObserver.awaitCount(1)
                    addOrUpdateOtherObserver.assertValue(
                            Pair(experienceList, ResultCacheFactory.AddPosition.START))
                }
            }
        }

        fun should_emit_that_list_through_kind_update() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    updateMineObserver.awaitCount(1)
                    updateMineObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    updateSavedObserver.awaitCount(1)
                    updateSavedObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    updateExploreObserver.awaitCount(1)
                    updateExploreObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.PERSONS -> {
                    updatePersonsObserver.awaitCount(1)
                    updatePersonsObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.OTHER -> {
                    updateOtherObserver.awaitCount(1)
                    updateOtherObserver.assertValue(experienceList)
                }
            }
        }

        fun should_emit_that_result_through_replace_result() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    replaceResultMineObserver.awaitCount(1)
                    replaceResultMineObserver.assertValue(experiencesResult)
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    replaceResultSavedObserver.awaitCount(1)
                    replaceResultSavedObserver.assertValue(experiencesResult)
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    replaceResultExploreObserver.awaitCount(1)
                    replaceResultExploreObserver.assertValue(experiencesResult)
                }
                ExperienceRepoSwitch.Kind.PERSONS -> {
                    replaceResultPersonsObserver.awaitCount(1)
                    replaceResultPersonsObserver.assertValue(experiencesResult)
                }
                ExperienceRepoSwitch.Kind.OTHER -> {
                    replaceResultOtherObserver.awaitCount(1)
                    replaceResultOtherObserver.assertValue(experiencesResult)
                }
            }
        }

        fun should_return_a_flowable_with_experience_only_once() {
            assertEquals(resultMineFlowable, switch.mineResultCache.resultFlowable)
            val testSubscriber = TestSubscriber.create<Result<Experience>>()
            resultExperienceFlowable.subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            testSubscriber.assertValue(ResultSuccess(experience))
        }

        fun should_return_result_with_not_cached_error() {
            assertEquals(resultMineFlowable, switch.mineResultCache.resultFlowable)
            val testSubscriber = TestSubscriber.create<Result<Experience>>()
            resultExperienceFlowable.subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            val result = testSubscriber.events[0][0] as Result<Experience>
            assertEquals(result.error, ExperienceRepoSwitch.NotCachedExperienceException())
            assertEquals(result, ResultError<Experience>(
                    ExperienceRepoSwitch.NotCachedExperienceException()))
            testSubscriber.assertValue(
                    ResultError(ExperienceRepoSwitch.NotCachedExperienceException()))
        }

        fun should_emit_that_action_through_kind_action_observer() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    testMineActionObserver.awaitCount(1)
                    testMineActionObserver.assertValue(Request(action, requestParams))
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    testSavedActionObserver.awaitCount(1)
                    testSavedActionObserver.assertValue(Request(action, requestParams))
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    testExploreActionObserver.awaitCount(1)
                    testExploreActionObserver.assertValue(Request(action, requestParams))
                }
                ExperienceRepoSwitch.Kind.PERSONS -> {
                    testPersonsActionObserver.awaitCount(1)
                    testPersonsActionObserver.assertValue(Request(action, requestParams))
                }
                ExperienceRepoSwitch.Kind.OTHER -> {}
            }
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
