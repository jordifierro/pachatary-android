package com.pachatary.data.experience

import com.pachatary.data.common.*
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.observers.TestObserver
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceRequesterFactoryTest {

    val requesterKindValues = arrayListOf(ExperienceRepoSwitch.Kind.MINE,
                                          ExperienceRepoSwitch.Kind.SAVED,
                                          ExperienceRepoSwitch.Kind.EXPLORE,
                                          ExperienceRepoSwitch.Kind.PERSONS)

    @Test
    fun test_get_firsts_does_nothing_when_result_loading() {
        for (kind in requesterKindValues) {
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
    fun test_get_firsts_emits_loading_and_calls_api_to_emit_its_result() {
        for (kind in requesterKindValues) {
            given {
                a_kind(kind)
                a_request_params()
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
    fun test_get_firsts_after_search_params_changed_reset_and_acts_like_normal_get_firsts() {
        given {
            a_kind(ExperienceRepoSwitch.Kind.EXPLORE)
            a_request_params()
            a_result_cache_that_returns_paginated_search()
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

    @Test
    fun test_paginate_does_nothing_if_not_there_are_more_elements() {
        for (kind in requesterKindValues) {
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
        for (kind in requesterKindValues) {
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
        for (kind in requesterKindValues) {
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
        for (kind in requesterKindValues) {
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
        for (kind in requesterKindValues) {
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
        for (kind in requesterKindValues) {
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

    @Test
    fun test_paginate_after_search_params_changed_reset_and_acts_like_normal_get_firsts() {
        given {
            a_kind(ExperienceRepoSwitch.Kind.EXPLORE)
            a_request_params()
            a_result_cache_that_returns_paginated_search()
            an_api_repo_that_returns_two_experiences()
        } whenn {
            create_requester()
            paginate()
        } then {
            should_emit_loading_through_replace_result_cache()
            should_call_api()
            should_replace_result_with_that_two_experiences_and_last_event_get_firsts()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        @Mock lateinit var mockApiRepository: ExperienceApiRepo
        lateinit var requesterFactory: ExperienceRequesterFactory
        lateinit var requesterObserver: Observer<Request>
        var kind = ExperienceRepoSwitch.Kind.MINE
        lateinit var resultCache: ResultCacheFactory.ResultCache<Experience>
        lateinit var resultFlowable: Flowable<Result<List<Experience>>>
        val addOrUpdateObserver = TestObserver.create<Pair<List<Experience>,
                                                           ResultCacheFactory.AddPosition>>()
        val updateObserver = TestObserver.create<List<Experience>>()
        val replaceResultObserver = TestObserver.create<Result<List<Experience>>>()
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        var nextUrl = ""
        val exception = Exception()
        var requestParams: Request.Params? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            requesterFactory = ExperienceRequesterFactory(mockApiRepository)

            return this
        }

        fun a_kind(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun a_request_params() {
            requestParams = Request.Params("culture", 2.8, -4.3, "usr.nm")
        }

        fun a_next_url() {
            this.nextUrl = "next-url"
        }

        fun a_result_cache_that_return_loading_result() {
            resultFlowable = Flowable.just(ResultInProgress())
        }

        fun a_result_cache_that_return_success_not_initial_result() {
            resultFlowable = Flowable.just(
                    ResultSuccess<List<Experience>>(null, action = Request.Action.GET_FIRSTS))
        }

        fun a_result_cache_that_return_initial_result() {
            resultFlowable = Flowable.just(
                    ResultSuccess<List<Experience>>(null, action = Request.Action.NONE))
        }

        fun a_result_cache_that_return_error_after_pagination_with_next_url() {
            resultFlowable = Flowable.just(
                    ResultError(Exception(), data = listOf(),
                                action = Request.Action.PAGINATE, nextUrl = nextUrl))
        }

        fun a_result_cache_that_return_two_experiences_and_next_url_from_get_firsts() {
            experienceA = Experience("1", "t", "d", null, true, true)
            experienceB = Experience("2", "t", "d", null, true, true)
            resultFlowable = Flowable.just(ResultSuccess(listOf(experienceA, experienceB),
                    action = Request.Action.GET_FIRSTS, nextUrl = nextUrl))
        }

        fun an_api_repo_that_returns_two_experiences() {
            experienceA = Experience("1", "t", "d", null, true, true)
            experienceB = Experience("2", "t", "d", null, true, true)
            when (kind) {
                ExperienceRepoSwitch.Kind.MINE ->
                    BDDMockito.given(mockApiRepository.myExperiencesFlowable())
                            .willReturn(Flowable.just(
                                    ResultSuccess(listOf(experienceA, experienceB))))
                ExperienceRepoSwitch.Kind.SAVED ->
                    BDDMockito.given(mockApiRepository.savedExperiencesFlowable())
                            .willReturn(Flowable.just(
                                    ResultSuccess(listOf(experienceA, experienceB))))
                ExperienceRepoSwitch.Kind.EXPLORE ->
                    BDDMockito.given(mockApiRepository.exploreExperiencesFlowable(
                        requestParams!!.word, requestParams!!.latitude, requestParams!!.longitude))
                            .willReturn(Flowable.just(
                                    ResultSuccess(listOf(experienceA, experienceB))))
                ExperienceRepoSwitch.Kind.PERSONS ->
                    BDDMockito.given(
                            mockApiRepository.personsExperienceFlowable(requestParams!!.username!!))
                            .willReturn(Flowable.just(
                                    ResultSuccess(listOf(experienceA, experienceB))))
                else -> fail()
            }
        }

        fun an_api_repo_that_returns_two_experiences_for_that_url_pagination() {
            experienceA = Experience("1", "t", "d", null, true, true)
            experienceB = Experience("2", "t", "d", null, true, true)
            BDDMockito.given(mockApiRepository.paginateExperiences(nextUrl))
                    .willReturn(Flowable.just(ResultSuccess(listOf(experienceA, experienceB))))
        }

        fun an_api_repo_that_returns_error_that_next_url() {
            BDDMockito.given(mockApiRepository.paginateExperiences(nextUrl))
                    .willReturn(Flowable.just(ResultError(exception, data = listOf())))
        }

        fun a_result_cache_that_return_result_that_has_no_more_elements() {
            resultFlowable = Flowable.just(ResultSuccess(listOf(), nextUrl = null))
        }

        fun a_result_cache_that_returns_paginated_search() {
            resultFlowable = Flowable.just(ResultSuccess(
                    listOf(), action = Request.Action.PAGINATE, params = Request.Params("other")))
        }

        fun a_result_cache_that_return_error_getting_firsts() {
            resultFlowable = Flowable.just(ResultError(Exception(), data = listOf(),
                                                       action = Request.Action.GET_FIRSTS))
        }

        fun a_result_cache_that_return_success_last_event_get_firsts_with_next_url() {
            resultFlowable = Flowable.just(
                    ResultSuccess(listOf(), action = Request.Action.GET_FIRSTS, nextUrl = nextUrl))
        }

        fun create_requester() {
            resultCache = ResultCacheFactory.ResultCache(replaceResultObserver,
                    addOrUpdateObserver, updateObserver, resultFlowable)
            requesterObserver = requesterFactory.create(resultCache, kind)
        }

        fun emit_get_firsts() {
            requesterObserver.onNext(Request(Request.Action.GET_FIRSTS, requestParams))
        }

        fun paginate() {
            requesterObserver.onNext(Request(Request.Action.PAGINATE, requestParams))
        }

        fun should_do_nothing() {
            replaceResultObserver.assertNoValues()
            addOrUpdateObserver.assertNoValues()
            updateObserver.assertNoValues()
            BDDMockito.then(mockApiRepository).shouldHaveZeroInteractions()
        }

        fun should_emit_loading_through_replace_result_cache() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(ResultInProgress(listOf<Experience>(), action = Request.Action.GET_FIRSTS,
                                          params = requestParams), result)
        }

        fun should_emit_loading_through_replace_result_cache_with_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(ResultInProgress(listOf<Experience>(),
                                          action = Request.Action.PAGINATE, nextUrl = nextUrl),
                         result)
        }

        fun should_emit_loading_through_replace_result_cache_with_that_experiences() {
            val result = replaceResultObserver.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(ResultInProgress(listOf(experienceA, experienceB),
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
                        requestParams!!.word, requestParams!!.latitude, requestParams!!.longitude)
                ExperienceRepoSwitch.Kind.PERSONS -> BDDMockito.then(mockApiRepository).should()
                            .personsExperienceFlowable(requestParams!!.username!!)
                else -> fail()
            }
        }

        fun should_call_api_paginate_with_next_url() {
            BDDMockito.then(mockApiRepository).should().paginateExperiences(nextUrl)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_get_firsts() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(ResultSuccess(listOf(experienceA, experienceB),
                                       action = Request.Action.GET_FIRSTS, params = requestParams),
                         result)
        }

        fun should_replace_result_with_that_two_experiences_and_last_event_paginate() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(ResultSuccess(listOf(experienceA, experienceB),
                                       action = Request.Action.PAGINATE), result)
        }

        fun should_replace_result_with_pagination_error_but_same_experiences() {
            val result = replaceResultObserver.events.get(0).get(1) as Result<List<Experience>>
            assertEquals(ResultError(exception, listOf(experienceA, experienceB),
                                     action = Request.Action.PAGINATE, nextUrl = nextUrl), result)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
