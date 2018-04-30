package com.pachatary.data.experience

import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultCacheFactory
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
                a_search_params()
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
    fun test_paginate_emits_in_progress_and_calls_next_url_after_pagination_error_when_success() {
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

    @Test
    fun test_paginate_emits_error_with_previous_data_when_api_returns_error() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                a_kind(kind)
                a_next_url()
                an_api_repo_that_returns_error_that_next_url()
                a_result_cache_that_return_two_experiences_and_next_url_from_get_firsts()
            } whenn {
                create_requester()
                paginate()
            } then {
                should_emit_loading_through_replace_result_cache_with_that_experiences()
                should_call_api_paginate_with_next_url()
                should_replace_result_with_pagination_error_but_same_experiences()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        lateinit var requesterFactory: ExperienceRequesterFactory
        lateinit var requesterObserver: Observer<Request>
        var kind = ExperienceRepoSwitch.Kind.MINE
        lateinit var resultCache: ResultCacheFactory.ResultCache<Experience>
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        val addOrUpdateObserver = TestObserver.create<List<Experience>>()
        val updateObserver = TestObserver.create<List<Experience>>()
        val replaceResultObserver = TestObserver.create<Result<List<Experience>>>()
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        var nextUrl = ""
        val exception = Exception()
        var searchParams: Request.Params? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            requesterFactory = ExperienceRequesterFactory(mockApiRepository)

            return this
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun a_search_params() {
            searchParams = Request.Params("culture", 2.8, -4.3)
        }

        fun a_next_url() {
            this.nextUrl = "next-url"
        }

        fun a_result_cache_that_return_loading_result() {
            resultFlowable = Flowable.just(Result<List<Experience>>(null, inProgress = true))
        }

        fun a_result_cache_that_return_success_not_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, action = Request.Action.GET_FIRSTS))
        }

        fun a_result_cache_that_return_initial_result() {
            resultFlowable = Flowable.just(
                    Result<List<Experience>>(null, action = Request.Action.NONE))
        }

        fun a_result_cache_that_return_error_after_pagination_with_next_url() {
            resultFlowable = Flowable.just( Result(listOf(), action = Request.Action.PAGINATE,
                                                   error = Exception(), nextUrl = nextUrl))
        }

        fun a_result_cache_that_return_two_experiences_and_next_url_from_get_firsts() {
            experienceA = Experience("1", "t", "d", null, true, true, "a")
            experienceB = Experience("2", "t", "d", null, true, true, "b")
            resultFlowable = Flowable.just(Result(listOf(experienceA, experienceB),
                    action = Request.Action.GET_FIRSTS, nextUrl = nextUrl))
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
                    BDDMockito.given(mockApiRepository.exploreExperiencesFlowable(
                        searchParams!!.word, searchParams!!.latitude, searchParams!!.longintude))
                            .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
            }
        }

        fun an_api_repo_that_returns_two_experiences_for_that_url_pagination() {
            experienceA = Experience("1", "t", "d", null, true, true, "a")
            experienceB = Experience("2", "t", "d", null, true, true, "b")
            BDDMockito.given(mockApiRepository.paginateExperiences(nextUrl))
                    .willReturn(Flowable.just(Result(listOf(experienceA, experienceB))))
        }

        fun an_api_repo_that_returns_error_that_next_url() {
            BDDMockito.given(mockApiRepository.paginateExperiences(nextUrl))
                    .willReturn(Flowable.just(Result(listOf(), error = exception)))
        }

        fun a_result_cache_that_return_result_that_has_no_more_elements() {
            resultFlowable = Flowable.just(Result(listOf(), nextUrl = null))
        }

        fun a_result_cache_that_return_error_getting_firsts() {
            resultFlowable = Flowable.just(Result(listOf(), error = Exception(),
                                                  action = Request.Action.GET_FIRSTS))
        }

        fun a_result_cache_that_return_success_last_event_get_firsts_with_next_url() {
            resultFlowable = Flowable.just(
                    Result(listOf(), action = Request.Action.GET_FIRSTS, nextUrl = nextUrl))
        }

        fun create_requester() {
            resultCache = ResultCacheFactory.ResultCache(replaceResultObserver,
                    addOrUpdateObserver, updateObserver, resultFlowable)
            requesterObserver = requesterFactory.create(resultCache, kind)
        }

        fun emit_get_firsts() {
            requesterObserver.onNext(Request(Request.Action.GET_FIRSTS, searchParams))
        }

        fun paginate() {
            requesterObserver.onNext(Request(Request.Action.PAGINATE))
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
                                action = Request.Action.GET_FIRSTS), result)
        }

        fun should_emit_loading_through_replace_result_cache_with_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(Result(listOf<Experience>(), inProgress = true,
                    action = Request.Action.PAGINATE, nextUrl = nextUrl),
                    result)
        }

        fun should_emit_loading_through_replace_result_cache_with_that_experiences() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB), inProgress = true,
                    action = Request.Action.PAGINATE, nextUrl = nextUrl),
                    result)
        }

        fun should_call_api() {
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE ->
                    BDDMockito.then(mockApiRepository).should().myExperiencesFlowable()
                ExperienceRepoSwitch.Kind.SAVED ->
                    BDDMockito.then(mockApiRepository).should().savedExperiencesFlowable()
                ExperienceRepoSwitch.Kind.EXPLORE ->
                    BDDMockito.then(mockApiRepository).should().exploreExperiencesFlowable(
                            searchParams!!.word, searchParams!!.latitude, searchParams!!.longintude)
            }
        }

        fun should_call_api_paginate_with_next_url() {
            BDDMockito.then(mockApiRepository).should().paginateExperiences(nextUrl)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_get_firsts() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB),
                                action = Request.Action.GET_FIRSTS), result)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB),
                    action = Request.Action.PAGINATE), result)
        }

        fun should_replace_result_with_pagination_error_but_same_experiences() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(Result(listOf(experienceA, experienceB), action = Request.Action.PAGINATE,
                    error = exception, nextUrl = nextUrl), result)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
