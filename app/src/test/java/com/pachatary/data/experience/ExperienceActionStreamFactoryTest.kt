package com.pachatary.data.experience

import com.pachatary.data.common.ResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceActionStreamFactoryTest {

    @Test
    fun test_get_firsts_does_nothing_when_result_loading() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_stream_that_return_loading_result()
            } whenn {
                create_action_stream()
                emit_get_firsts()
            } then {
                should_do_nothing()
            }
        }
    }

    @Test
    fun test_get_firsts_does_nothing_when_result_has_been_initialized_and_has_no_errors() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_stream_that_return_success_not_initial_result()
            } whenn {
                create_action_stream()
                emit_get_firsts()
            } then {
                should_do_nothing()
            }
        }
    }

    @Test
    fun test_get_firsts_emits_loading_and_calls_api_to_emit_its_result() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_stream_that_return_initial_result()
                an_api_repo_that_returns_two_experiences()
            } whenn {
                create_action_stream()
                emit_get_firsts()
            } then {
                should_emit_loading_through_replace_result_stream()
                should_call_api()
                should_replace_result_with_that_two_experiences_and_last_event_get_firsts()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        lateinit var actionStreamFactory: ExperienceActionStreamFactory
        lateinit var actionStreamObserver: Observer<ExperienceActionStreamFactory.Action>
        var kind = ExperienceRepoSwitch.Kind.MINE
        lateinit var resultStream: ResultStreamFactory.ResultStream<Experience>
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        val addOrUpdateObserver = TestObserver.create<List<Experience>>()
        val updateObserver = TestObserver.create<List<Experience>>()
        val replaceResultObserver = TestObserver.create<Result<List<Experience>>>()
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            actionStreamFactory = ExperienceActionStreamFactory(mockApiRepository)

            return this
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun a_result_stream_that_return_loading_result() {
            resultFlowable = Flowable.just(Result<List<Experience>>(null, inProgress = true))
        }

        fun a_result_stream_that_return_success_not_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, lastEvent = Result.Event.GET_FIRSTS))
        }

        fun a_result_stream_that_return_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, lastEvent = Result.Event.NONE))
        }

        fun an_api_repo_that_returns_two_experiences() {
            experienceA = Experience("1", "t", "d", null, true, true, "a")
            experienceB = Experience("2", "t", "d", null, true, true, "b")
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE ->
                    BDDMockito.given(mockApiRepository.myExperiencesFlowable())
                            .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
                ExperienceRepoSwitch.Kind.SAVED ->
                    BDDMockito.given(mockApiRepository.savedExperiencesFlowable())
                            .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
                ExperienceRepoSwitch.Kind.EXPLORE ->
                    BDDMockito.given(mockApiRepository.exploreExperiencesFlowable())
                            .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
            }
        }

        fun create_action_stream() {
            resultStream = ResultStreamFactory.ResultStream(replaceResultObserver,
                    addOrUpdateObserver, updateObserver, resultFlowable)
            actionStreamObserver = actionStreamFactory.create(resultStream, kind)
        }

        fun emit_get_firsts() {
            actionStreamObserver.onNext(ExperienceActionStreamFactory.Action.GET_FIRSTS)
        }

        fun should_do_nothing() {
            replaceResultObserver.assertNoValues()
            addOrUpdateObserver.assertNoValues()
            updateObserver.assertNoValues()
            BDDMockito.then(mockApiRepository).shouldHaveZeroInteractions()
        }

        fun should_emit_loading_through_replace_result_stream() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(Result(listOf<Experience>(), inProgress = true), result)
        }

        fun should_call_api() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE -> BDDMockito.then(mockApiRepository).should()
                        .myExperiencesFlowable()
                ExperienceRepoSwitch.Kind.SAVED -> BDDMockito.then(mockApiRepository).should()
                        .savedExperiencesFlowable()
                ExperienceRepoSwitch.Kind.EXPLORE -> BDDMockito.then(mockApiRepository).should()
                        .exploreExperiencesFlowable()
            }
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_get_firsts() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB),
                                lastEvent = Result.Event.GET_FIRSTS), result)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
