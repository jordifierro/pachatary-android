package com.pachatary.data.experience

import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.*
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceRepositoryTest {

    @Test
    fun test_experiences_flowable_returns_result_flowable_for_mine_explore_and_persons() {
        for (kind in listOf(ExperienceRepoSwitch.Kind.MINE,
                            ExperienceRepoSwitch.Kind.EXPLORE,
                            ExperienceRepoSwitch.Kind.PERSONS,
                            ExperienceRepoSwitch.Kind.OTHER)) {
            given {
                a_flowable()
                kind_of_experiences(kind)
                repo_switch_that_returns_that_flowable_for_that_kind()
            } whenn {
                experiences_flowable_is_called()
            } then {
                should_call_repo_switch_get_result_flowable_with_corresponding_kind()
                should_return_that_flowable()
            }
        }
    }

    @Test
    fun test_experiences_flowable_filter_not_saved_experiences_when_saved_kind() {
        given {
            a_saved_experience()
            a_non_saved_experience()
            a_flowable_that_returns_that_experiences()
            kind_of_experiences(ExperienceRepoSwitch.Kind.SAVED)
            repo_switch_that_returns_that_flowable_for_that_kind()
        } whenn {
            experiences_flowable_is_called()
        } then {
            should_call_repo_switch_get_result_flowable_with_corresponding_kind()
            should_filter_non_saved_experiences_on_subscribe()
        }
    }

    @Test
    fun test_experience_flowable_returns_switchers_one() {
        given {
            an_experience_flowable()
            an_experience_id()
            a_repo_switch_that_returns_that_flowable_when_experiences_id()
        } whenn {
            experience_flowable_is_called()
        } then {
            should_call_repo_switch_get_experience_flowable_with_that_id()
            should_return_that_experience_flowable()
        }
    }

    @Test
    fun test_when_experience_flowable_returns_not_cached_error_calls_api_and_updates_other_cache() {
        given {
            an_experience_flowable()
            an_experience_id()
            an_experience()
            an_api_repo_that_return_that_experience_on_get()
            a_repo_switch_that_returns_not_cached_exception()
        } whenn {
            experience_flowable_is_called()
        } then {
            should_call_repo_switch_get_experience_flowable_with_that_id()
            should_call_api_repo_get_experience_with_id()
            should_call_switch_add_or_update_with_that_experience_to_other_cache()
            should_return_that_experience()
        }
    }

    @Test
    fun test_create_experience_call_create_experience_and_update_mine() {
        given {
            an_experience()
            an_experience_repo_that_returns_that_experience_when_create()
        } whenn {
            create_experience_is_called()
        } then {
            should_call_api_repo_create_experience()
            should_call_switch_add_or_update_with_that_experience()
            should_return_a_flowable_with_that_experience()
        }
    }

    @Test
    fun test_edit_experience_call_edit_experience_and_update_mine() {
        given {
            an_experience()
            an_experience_repo_that_returns_that_experience_when_edit()
        } whenn {
            edit_experience_is_called()
        } then {
            should_call_api_repo_edit_experience()
            should_call_switch_add_or_update_with_that_experience()
            should_return_a_flowable_with_that_experience()
        }
    }

    @Test
    fun test_upload_experience_picture_call_upload_picture_experience_and_update_mine() {
        given {
            an_experience_id()
            an_image_cropped_string()
        } whenn {
            upload_experience_picture_is_called()
        } then {
            should_call_api_repo_upload_picture()
        }
    }

    @Test
    fun test_save_experience_call_save_experience_and_updates_saved_explore_and_persons() {
        given {
            an_experience_id()
            an_experience_repo_that_returns_a_publisher_when_save_is_called()
            a_non_saved_experience()
            a_repo_switch_that_returns_non_saved_experience_when_call_with_experience_id()
        } whenn {
            save_experience_is_called()
        } then {
            should_call_api_repo_save_experience()
            should_modify_experience_to_saved_and_update_saved_explore_and_persons_cache()
            should_subscribe_to_publisher_returned_by_api()
        }
    }

    @Test
    fun test_unsave_experience_call_unsave_experience_and_updates_saved_and_explore() {
        given {
            an_experience_id()
            an_experience_repo_that_returns_a_publisher_when_unsave_is_called()
            a_saved_experience()
            a_repo_switch_that_returns_saved_experience_when_call_with_experience_id()
        } whenn {
            unsave_experience_is_called()
        } then {
            should_call_api_repo_unsave_experience()
            should_modify_experience_to_unsaved_and_update_saved_explore_and_persons_cache()
            should_subscribe_to_publisher_returned_by_api()
        }
    }

    @Test
    fun test_get_first_experiences_emits_action_through_switch() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                kind_of_experiences(kind)
                a_search_params()
            } whenn {
                get_first_experiences_is_called()
            } then {
                should_call_switch_execute_get_firsts_action()
            }
        }
    }

    @Test
    fun test_get_more_experiences_emits_paginate_action_through_switch() {
        for (kind in ExperienceRepoSwitch.Kind.values()) {
            given {
                kind_of_experiences(kind)
            } whenn {
                get_more_experiences_is_called()
            } then {
                should_call_switch_execute_paginate_action()
            }
        }
    }

    @Test
    fun test_translate_share_id_returns_apis_flowable() {
        given {
            an_experience_share_id()
            an_api_repo_that_returns_string_flowable_when_translate_share_id()
        } whenn {
            translate_share_id()
        } then {
            should_call_api_repo_translate_share_id_with_share_id()
            should_return_string_flowable()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        lateinit var repository: ExperienceRepository
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        @Mock lateinit var mockExperiencesRepoSwitch: ExperienceRepoSwitch
        lateinit var experiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var experienceFlowable: Flowable<Result<Experience>>
        var kind = ExperienceRepoSwitch.Kind.MINE
        lateinit var resultExperiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var resultExperienceFlowable: Flowable<Result<Experience>>
        val testExperienceSubscriber = TestSubscriber.create<Result<Experience>>()
        val saveExperiencePublisher = PublishSubject.create<Result<Void>>()
        lateinit var nonSavedExperience: Experience
        lateinit var savedExperience: Experience
        lateinit var experience: Experience
        var experienceId = ""
        var experienceShareId = ""
        var croppedImageString = ""
        lateinit var searchParams: Request.Params
        lateinit var stringFlowable: Flowable<Result<String>>
        lateinit var stringFlowableResult: Flowable<Result<String>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = ExperienceRepository(mockApiRepository, mockExperiencesRepoSwitch)

            return this
        }

        fun an_experience_share_id() {
            experienceShareId = "sd34ErT5"
        }

        fun a_search_params() {
            searchParams = Request.Params("c", 8.5, -0.3)
        }

        fun a_flowable() {
            experiencesFlowable = Flowable.empty()
        }

        fun an_experience_flowable() {
            experienceFlowable = Flowable.empty()
        }

        fun an_experience() {
            experience = Experience("1", "t", "d", null, true, true, "")
        }

        fun an_image_cropped_string() {
            croppedImageString = "image_url"
        }

        fun an_experience_repo_that_returns_a_publisher_when_save_is_called() {
            BDDMockito.given(mockApiRepository.saveExperience(true, experienceId))
                    .willReturn(saveExperiencePublisher.toFlowable(BackpressureStrategy.LATEST))
        }

        fun an_experience_repo_that_returns_a_publisher_when_unsave_is_called() {
            BDDMockito.given(mockApiRepository.saveExperience(false, experienceId))
                    .willReturn(saveExperiencePublisher.toFlowable(BackpressureStrategy.LATEST))
        }

        fun an_experience_repo_that_returns_that_experience_when_create() {
            BDDMockito.given(mockApiRepository.createExperience(experience))
                    .willReturn(Flowable.just(ResultSuccess(experience)))
        }

        fun an_experience_repo_that_returns_that_experience_when_edit() {
            BDDMockito.given(mockApiRepository.editExperience(experience))
                    .willReturn(Flowable.just(ResultSuccess(experience)))
        }

        fun an_api_repo_that_return_that_experience_on_get() {
            BDDMockito.given(mockApiRepository.experienceFlowable(experienceId))
                    .willReturn(Flowable.just(ResultSuccess(experience)))
        }

        fun an_api_repo_that_returns_string_flowable_when_translate_share_id() {
            stringFlowable = Flowable.empty()
            BDDMockito.given(mockApiRepository.translateShareId(experienceShareId))
                    .willReturn(stringFlowable)
        }

        fun a_saved_experience() {
            savedExperience = Experience("1", "t", "d", null, false, true, "")
        }

        fun a_non_saved_experience() {
            nonSavedExperience = Experience("1", "t", "d", null, false, false, "")
        }

        fun a_flowable_that_returns_that_experiences() {
            experiencesFlowable = Flowable.just(ResultSuccess(listOf(savedExperience, nonSavedExperience)))
        }

        fun kind_of_experiences(kind: ExperienceRepoSwitch.Kind) {
            this.kind = kind
        }

        fun an_experience_id() {
            experienceId = "4"
        }

        fun a_repo_switch_that_returns_that_flowable_when_experiences_id() {
            BDDMockito.given(mockExperiencesRepoSwitch.getExperienceFlowable(experienceId))
                    .willReturn(experienceFlowable)
        }

        fun a_repo_switch_that_returns_not_cached_exception() {
            BDDMockito.given(mockExperiencesRepoSwitch.getExperienceFlowable(experienceId))
                    .willReturn(Flowable.just(
                            ResultError(ExperienceRepoSwitch.NotCachedExperienceException())))
        }

        fun a_repo_switch_that_returns_non_saved_experience_when_call_with_experience_id() {
            BDDMockito.given(mockExperiencesRepoSwitch.getExperienceFlowable(experienceId))
                    .willReturn(Flowable.just(ResultSuccess(nonSavedExperience)))
        }

        fun a_repo_switch_that_returns_saved_experience_when_call_with_experience_id() {
            BDDMockito.given(mockExperiencesRepoSwitch.getExperienceFlowable(experienceId))
                    .willReturn(Flowable.just(ResultSuccess(savedExperience)))
        }

        fun repo_switch_that_returns_that_flowable_for_that_kind() {
            BDDMockito.given(mockExperiencesRepoSwitch.getResultFlowable(kind))
                    .willReturn(experiencesFlowable)
        }

        fun experiences_flowable_is_called() {
            resultExperiencesFlowable = repository.experiencesFlowable(kind)
        }

        fun create_experience_is_called() {
            repository.createExperience(experience).subscribe(testExperienceSubscriber)
        }

        fun edit_experience_is_called() {
            repository.editExperience(experience).subscribe(testExperienceSubscriber)
        }

        fun get_first_experiences_is_called() {
            repository.getFirstExperiences(kind, searchParams)
        }

        fun get_more_experiences_is_called() {
            repository.getMoreExperiences(kind)
        }

        fun translate_share_id() {
            stringFlowableResult = repository.translateShareId(experienceShareId)
        }

        fun upload_experience_picture_is_called() {
            repository.uploadExperiencePicture(experienceId, croppedImageString)
        }

        fun save_experience_is_called() {
            repository.saveExperience(experienceId, true)
        }

        fun unsave_experience_is_called() {
            repository.saveExperience(experienceId, false)
        }

        fun experience_flowable_is_called() {
            resultExperienceFlowable = repository.experienceFlowable(experienceId)
            resultExperienceFlowable.subscribe(testExperienceSubscriber)
        }

        fun should_call_repo_switch_get_experience_flowable_with_that_id() {
            BDDMockito.then(mockExperiencesRepoSwitch).should().getExperienceFlowable(experienceId)
        }

        fun should_return_that_experience_flowable() {
            assertEquals(experienceFlowable, resultExperienceFlowable)
        }

        fun should_call_repo_switch_get_result_flowable_with_corresponding_kind() {
            BDDMockito.then(mockExperiencesRepoSwitch).should().getResultFlowable(kind)
        }

        fun should_return_that_flowable() {
            assertEquals(experiencesFlowable, resultExperiencesFlowable)
        }

        fun should_filter_non_saved_experiences_on_subscribe() {
            val testSubscriber = TestSubscriber.create<Result<List<Experience>>>()
            resultExperiencesFlowable.subscribe(testSubscriber)
            testSubscriber.awaitCount(1)

            val result = testSubscriber.events.get(0).get(0) as Result<List<Experience>>
            assertEquals(listOf(savedExperience), result.data!!)
        }

        fun should_call_api_repo_create_experience() {
            BDDMockito.then(mockApiRepository).should().createExperience(experience)
        }

        fun should_call_api_repo_edit_experience() {
            BDDMockito.then(mockApiRepository).should().editExperience(experience)
        }

        fun should_call_switch_add_or_update_with_that_experience() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.MINE,
                                  ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                  listOf(experience))
        }

        fun should_return_a_flowable_with_that_experience() {
            testExperienceSubscriber.awaitCount(1)
            val result = testExperienceSubscriber.events.get(0).get(0) as Result<Experience>
            assertEquals(experience, result.data)
        }

        fun should_call_api_repo_upload_picture() {
            BDDMockito.then(mockApiRepository).should()
                    .uploadExperiencePicture(experienceId, croppedImageString,
                                             repository.addOrUpdateExperienceToMine)
        }

        fun should_call_api_repo_save_experience() {
            BDDMockito.then(mockApiRepository).should().saveExperience(true, experienceId)
        }

        fun should_call_api_repo_unsave_experience() {
            BDDMockito.then(mockApiRepository).should().saveExperience(false, experienceId)
        }

        fun should_modify_experience_to_saved_and_update_saved_explore_and_persons_cache() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                                  ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                  list = listOf(nonSavedExperience.builder()
                                                                    .isSaved(true)
                                                      .savesCount(nonSavedExperience.savesCount + 1)
                                                                    .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(nonSavedExperience.builder()
                                                                .isSaved(true)
                                                      .savesCount(nonSavedExperience.savesCount + 1)
                                                                .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.PERSONS,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(nonSavedExperience.builder()
                                    .isSaved(true)
                                    .savesCount(nonSavedExperience.savesCount + 1)
                                    .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(nonSavedExperience.builder()
                                    .isSaved(true)
                                    .savesCount(nonSavedExperience.savesCount + 1)
                                    .build()))
        }

        fun should_modify_experience_to_unsaved_and_update_saved_explore_and_persons_cache() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                            ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                            list = listOf(savedExperience.builder()
                                    .isSaved(false)
                                    .savesCount(savedExperience.savesCount - 1)
                                    .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(savedExperience.builder()
                                    .isSaved(false)
                                    .savesCount(savedExperience.savesCount - 1)
                                    .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.PERSONS,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(savedExperience.builder()
                                    .isSaved(false)
                                    .savesCount(savedExperience.savesCount - 1)
                                    .build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(savedExperience.builder()
                                    .isSaved(false)
                                    .savesCount(savedExperience.savesCount - 1)
                                    .build()))
        }

        fun should_subscribe_to_publisher_returned_by_api() {
            assertTrue(saveExperiencePublisher.hasObservers())
        }

        fun should_call_switch_execute_get_firsts_action() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .executeAction(kind, Request.Action.GET_FIRSTS, searchParams)
        }

        fun should_call_switch_execute_paginate_action() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .executeAction(kind, Request.Action.PAGINATE)
        }

        fun should_call_api_repo_get_experience_with_id() {
            BDDMockito.then(mockApiRepository).should().experienceFlowable(experienceId)
        }

        fun should_call_switch_add_or_update_with_that_experience_to_other_cache() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.OTHER,
                                  ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                  listOf(experience))
        }

        fun should_return_that_experience() {
            resultExperienceFlowable.subscribe(testExperienceSubscriber)
            testExperienceSubscriber.awaitCount(1)
            testExperienceSubscriber.assertValue(ResultSuccess(experience))
        }

        fun should_call_api_repo_translate_share_id_with_share_id() {
            BDDMockito.then(mockApiRepository).should().translateShareId(experienceShareId)
        }

        fun should_return_string_flowable() {
            assertEquals(stringFlowableResult, stringFlowable)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
