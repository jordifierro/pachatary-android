package com.pachatary.data.experience

import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceRequesterFactoryTest {

    @Test
    fun test_get_firsts_does_nothing_when_result_loading() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_cache_that_return_loading_result()
            } whenn {
                create_requester()
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
                a_result_cache_that_return_success_not_initial_result()
            } whenn {
                create_requester()
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
                a_result_cache_that_return_initial_result()
                an_api_repo_that_returns_two_experiences()
            } whenn {
                create_requester()
                emit_get_firsts()
            } then {
                should_emit_loading_through_replace_result_cache()
                should_call_api()
                should_replace_result_with_that_two_experiences_and_last_event_get_firsts()
            }
        }
    }

    @Test
    fun test_paginate_does_nothing_if_not_there_are_more_elements() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_cache_that_return_result_that_has_no_more_elements()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_do_nothing()
            }
        }
    }

    @Test
    fun test_paginate_does_nothing_if_in_progress() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_cache_that_return_loading_result()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_do_nothing()
            }
        }
    }

    @Test
    fun test_paginate_does_nothing_if_error_getting_firsts() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_result_cache_that_return_error_getting_firsts()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_do_nothing()
            }
        }
    }

    @Test
    fun test_paginate_emits_in_progress_and_calls_next_url_when_initialized_and_success() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_next_url()
                an_api_repo_that_returns_two_experiences_for_that_url_pagination()
                a_result_cache_that_return_success_last_event_get_firsts_with_next_url()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_emit_loading_through_replace_result_cache_with_last_event_paginate()
                should_call_api_paginate_with_next_url()
                should_replace_result_with_that_two_experiences_and_last_event_paginate()
            }
        }
    }

    @Test
    fun test_paginate_emits_in_progress_and_calls_next_url_after_pagination_error() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_next_url()
                an_api_repo_that_returns_two_experiences_for_that_url_pagination()
                a_result_cache_that_return_error_after_pagination_with_next_url()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_emit_loading_through_replace_result_cache_with_last_event_paginate()
                should_call_api_paginate_with_next_url()
                should_replace_result_with_that_two_experiences_and_last_event_paginate()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        lateinit var requesterFactory: ExperienceRequesterFactory
        lateinit var requesterObserver: Observer<ExperienceRequesterFactory.Action>
        var kind = ExperienceRepoSwitch.Kind.MINE
        lateinit var resultCache: ResultCacheFactory.ResultCache<Experience>
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        val addOrUpdateObserver = TestObserver.create<List<Experience>>()
        val updateObserver = TestObserver.create<List<Experience>>()
        val replaceResultObserver = TestObserver.create<Result<List<Experience>>>()
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        var nextUrl = ""

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            requesterFactory = ExperienceRequesterFactory(mockApiRepository)

            return this
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun a_next_url() {
            this.nextUrl = "next-url"
        }

        fun a_result_cache_that_return_loading_result() {
            resultFlowable = Flowable.just(Result<List<Experience>>(null, inProgress = true))
        }

        fun a_result_cache_that_return_success_not_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, lastEvent = Result.Event.GET_FIRSTS))
        }

        fun a_result_cache_that_return_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, lastEvent = Result.Event.NONE))
        }

        fun a_result_cache_that_return_error_after_pagination_with_next_url() {
            resultFlowable = Flowable.just( Result(listOf(), lastEvent = Result.Event.PAGINATE,
                                                   error = Exception(), nextUrl = nextUrl))
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

        fun an_api_repo_that_returns_two_experiences_for_that_url_pagination() {
            experienceA = Experience("1", "t", "d", null, true, true, "a")
            experienceB = Experience("2", "t", "d", null, true, true, "b")
            BDDMockito.given(mockApiRepository.paginateExperiences(nextUrl))
                    .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
        }

        fun a_result_cache_that_return_result_that_has_no_more_elements() {
            resultFlowable = Flowable.just(Result(listOf(), nextUrl = null))
        }

        fun a_result_cache_that_return_error_getting_firsts() {
            resultFlowable = Flowable.just(Result(listOf(), error = Exception(),
                                                  lastEvent = Result.Event.GET_FIRSTS))
        }

        fun a_result_cache_that_return_success_last_event_get_firsts_with_next_url() {
            resultFlowable = Flowable.just(
                    Result(listOf(), lastEvent = Result.Event.GET_FIRSTS, nextUrl = nextUrl))
        }

        fun create_requester() {
            resultCache = ResultCacheFactory.ResultCache(replaceResultObserver,
                    addOrUpdateObserver, updateObserver, resultFlowable)
            requesterObserver = requesterFactory.create(resultCache, kind)
        }

        fun emit_get_firsts() {
            requesterObserver.onNext(ExperienceRequesterFactory.Action.GET_FIRSTS)
        }

        fun paginate() {
            requesterObserver.onNext(ExperienceRequesterFactory.Action.PAGINATE)
        }

        fun should_do_nothing() {
            replaceResultObserver.assertNoValues()
            addOrUpdateObserver.assertNoValues()
            updateObserver.assertNoValues()
            BDDMockito.then(mockApiRepository).shouldHaveZeroInteractions()
        }

        fun should_emit_loading_through_replace_result_cache() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(Result(listOf<Experience>(), inProgress = true,
                                lastEvent = Result.Event.GET_FIRSTS), result)
        }

        fun should_emit_loading_through_replace_result_cache_with_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(Result(listOf<Experience>(), inProgress = true,
                    lastEvent = Result.Event.PAGINATE, nextUrl = nextUrl),
                    result)
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

        fun should_call_api_paginate_with_next_url() {
            BDDMockito.then(mockApiRepository).should().paginateExperiences(nextUrl)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_get_firsts() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB),
                                lastEvent = Result.Event.GET_FIRSTS), result)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB),
                    lastEvent = Result.Event.PAGINATE), result)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
