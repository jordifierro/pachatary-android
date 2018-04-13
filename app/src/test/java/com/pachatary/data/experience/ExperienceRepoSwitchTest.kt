package com.pachatary.data.experience

import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
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
    fun test_get_experience_flowable_joins_three_stream_and_filters_experience_by_id() {
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
    fun test_execute_action_emits_through_appropiate_observer() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            for(action in ExperienceActionStreamFactory.Action.values()) {
                given {
                    a_kind(kind)
                    an_action(action)
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
        @Mock lateinit var resultStreamFactory: NewResultStreamFactory<Experience>
        @Mock lateinit var actionStreamFactory: ExperienceActionStreamFactory
        lateinit var switch: ExperienceRepoSwitch

        val addOrUpdateMineObserver = TestObserver.create<List<Experience>>()
        val updateMineObserver = TestObserver.create<List<Experience>>()
        val replaceResultMineObserver = TestObserver.create<Result<List<Experience>>>()
        var resultMineFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultMineStream = NewResultStreamFactory.ResultStream(replaceResultMineObserver,
                addOrUpdateMineObserver, updateMineObserver, resultMineFlowable)

        val addOrUpdateSavedObserver = TestObserver.create<List<Experience>>()
        val updateSavedObserver = TestObserver.create<List<Experience>>()
        val replaceResultSavedObserver = TestObserver.create<Result<List<Experience>>>()
        var resultSavedFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultSavedStream = NewResultStreamFactory.ResultStream(replaceResultSavedObserver,
                addOrUpdateSavedObserver, updateSavedObserver, resultSavedFlowable)

        val addOrUpdateExploreObserver = TestObserver.create<List<Experience>>()
        val updateExploreObserver = TestObserver.create<List<Experience>>()
        val replaceResultExploreObserver = TestObserver.create<Result<List<Experience>>>()
        var resultExploreFlowable = Flowable.empty<Result<List<Experience>>>()
        var resultExploreStream = NewResultStreamFactory.ResultStream(replaceResultExploreObserver,
                addOrUpdateExploreObserver, updateExploreObserver, resultExploreFlowable)

        val testMineActionObserver = TestObserver.create<ExperienceActionStreamFactory.Action>()
        val testSavedActionObserver = TestObserver.create<ExperienceActionStreamFactory.Action>()
        val testExploreActionObserver = TestObserver.create<ExperienceActionStreamFactory.Action>()

        var kind = ExperienceRepoSwitch.Kind.MINE
        var action = ExperienceActionStreamFactory.Action.GET_FIRSTS
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        lateinit var experienceList: List<Experience>
        var experiencesResult: Result<List<Experience>>? = null
        var modification = ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST
        var experienceId = ""
        lateinit var experience: Experience
        lateinit var resultExperienceFlowable: Flowable<Result<Experience>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            return this
        }

        fun initialize_switch() {
            resultMineStream = NewResultStreamFactory.ResultStream(replaceResultMineObserver,
                    addOrUpdateMineObserver, updateMineObserver, resultMineFlowable)
            resultSavedStream = NewResultStreamFactory.ResultStream(replaceResultSavedObserver,
                    addOrUpdateSavedObserver, updateSavedObserver, resultSavedFlowable)
            resultExploreStream = NewResultStreamFactory.ResultStream(replaceResultExploreObserver,
                    addOrUpdateExploreObserver, updateExploreObserver, resultExploreFlowable)
            BDDMockito.given(resultStreamFactory.create())
                    .willReturn(resultMineStream)
                    .willReturn(resultSavedStream)
                    .willReturn(resultExploreStream)
            BDDMockito.given(
                    actionStreamFactory.create(resultMineStream, ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(testMineActionObserver)
            BDDMockito.given(
                    actionStreamFactory.create(resultSavedStream, ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(testSavedActionObserver)
            BDDMockito.given(actionStreamFactory.create(resultExploreStream,
                    ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(testExploreActionObserver)

            switch = ExperienceRepoSwitch(resultStreamFactory, actionStreamFactory)
        }

        fun an_experience_id() {
            experienceId = "8"
        }

        fun an_experience_with_that_id() {
            experience = Experience(experienceId, "t", "", null, true, true, "")
        }

        fun some_result_flowables_that_emit_different_and_repeated_experiences() {
            resultMineFlowable = Flowable.just(Result(listOf(experience,
                    Experience("1", "", "", null, true, false, ""))))
            resultSavedFlowable = Flowable.just(Result(listOf(experience,
                    Experience("4", "", "", null, true, false, ""))))
            resultExploreFlowable = Flowable.just(Result(listOf(
                    Experience("4", "", "", null, true, false, ""),
                    Experience("4", "", "", null, true, false, ""))))
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun an_action(action: ExperienceActionStreamFactory.Action) {
            this.action = action
        }

        fun a_list_of_experiences() {
            experienceList = listOf(Experience("1", "t", "d", null, false, false, "a"),
                                    Experience("2", "t", "d", null, true, false, "b"))
        }

        fun a_result_with_that_list() {
            experiencesResult = Result(experienceList)
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
            switch.executeAction(kind, action)
        }

        fun result_should_be_kind_result_flowable() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> assertEquals(resultMineFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.SAVED -> assertEquals(resultSavedFlowable, resultFlowable)
                ExperienceRepoSwitch.Kind.EXPLORE ->
                    assertEquals(resultExploreFlowable, resultFlowable)
            }
        }

        fun should_emit_that_list_through_kind_add_or_update() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    addOrUpdateMineObserver.awaitCount(1)
                    addOrUpdateMineObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    addOrUpdateSavedObserver.awaitCount(1)
                    addOrUpdateSavedObserver.assertValue(experienceList)
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    addOrUpdateExploreObserver.awaitCount(1)
                    addOrUpdateExploreObserver.assertValue(experienceList)
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
            }
        }

        fun should_return_a_flowable_with_experience_only_once() {
            assertEquals(resultMineFlowable, switch.mineResultStream.resultFlowable)
            val testSubscriber = TestSubscriber.create<Result<Experience>>()
            resultExperienceFlowable.subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            testSubscriber.assertValue(Result(experience))
        }

        fun should_emit_that_action_through_kind_action_observer() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> {
                    testMineActionObserver.awaitCount(1)
                    testMineActionObserver.assertValue(action)
                }
                ExperienceRepoSwitch.Kind.SAVED -> {
                    testSavedActionObserver.awaitCount(1)
                    testSavedActionObserver.assertValue(action)
                }
                ExperienceRepoSwitch.Kind.EXPLORE -> {
                    testExploreActionObserver.awaitCount(1)
                    testExploreActionObserver.assertValue(action)
                }
            }
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
