package com.pachatary.data.experience

import com.pachatary.data.common.Result
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
    fun test_experiences_flowable_returns_result_flowable_from_switcher_for_mine_and_explore() {
        for (kind in listOf(ExperienceRepoSwitch.Kind.MINE, ExperienceRepoSwitch.Kind.EXPLORE)) {
            given {
                a_flowable()
                kind_of_experiences(kind)
                repo_switch_that_returns_that_flowable_for_that_kind()
            } whenn {
                experiences_flowable_is_called()
            } then {
                should_call_repo_switch_get_result_flowable_with_kind_mine()
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
            should_call_repo_switch_get_result_flowable_with_kind_mine()
            should_filter_non_saved_experiences_on_subscribe()
        }
    }

    @Test
    fun test_experience_flowable_returns_swichers_one() {
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
    fun test_updload_experience_picture_call_upload_picture_experience_and_update_mine() {
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
    fun test_save_experience_call_save_experience_and_updates_saved_and_explore() {
        given {
            an_experience_id()
            an_experience_repo_that_returns_a_publisher_when_save_is_called()
            a_non_saved_experience()
            a_repo_switch_that_returns_that_experience_when_call_with_experience_id()
        } whenn {
            save_experience_is_called()
        } then {
            should_call_api_repo_save_experience()
            should_modify_experience_to_saved_and_update_saved_and_explore_streams()
            should_subscribe_to_publisher_returned_by_api()
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
        var croppedImageString = ""

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = ExperienceRepository(mockApiRepository, mockExperiencesRepoSwitch)

            return this
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

        fun an_experience_repo_that_returns_that_experience_when_create() {
            BDDMockito.given(mockApiRepository.createExperience(experience))
                    .willReturn(Flowable.just(Result(experience)))
        }

        fun an_experience_repo_that_returns_that_experience_when_edit() {
            BDDMockito.given(mockApiRepository.editExperience(experience))
                    .willReturn(Flowable.just(Result(experience)))
        }

        fun a_saved_experience() {
            savedExperience = Experience("1", "t", "d", null, false, true, "")
        }

        fun a_non_saved_experience() {
            nonSavedExperience = Experience("1", "t", "d", null, false, false, "")
        }

        fun a_flowable_that_returns_that_experiences() {
            experiencesFlowable = Flowable.just(Result(listOf(savedExperience, nonSavedExperience)))
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

        fun a_repo_switch_that_returns_that_experience_when_call_with_experience_id() {
            BDDMockito.given(mockExperiencesRepoSwitch.getExperienceFlowable(experienceId))
                    .willReturn(Flowable.just(Result(nonSavedExperience)))
        }

        fun repo_switch_that_returns_that_flowable_for_that_kind() {
            BDDMockito.given(mockExperiencesRepoSwitch.getResultFlowable(kind)).willReturn(experiencesFlowable)
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

        fun upload_experience_picture_is_called() {
            repository.uploadExperiencePicture(experienceId, croppedImageString)
        }

        fun save_experience_is_called() {
            repository.saveExperience(experienceId, true)
        }

        fun experience_flowable_is_called() {
            resultExperienceFlowable = repository.experienceFlowable(experienceId)
        }

        fun should_call_repo_switch_get_experience_flowable_with_that_id() {
            BDDMockito.then(mockExperiencesRepoSwitch).should().getExperienceFlowable(experienceId)
        }

        fun should_return_that_experience_flowable() {
            assertEquals(experienceFlowable, resultExperienceFlowable)
        }

        fun should_call_repo_switch_get_result_flowable_with_kind_mine() {
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

        fun should_modify_experience_to_saved_and_update_saved_and_explore_streams() {
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.SAVED,
                                  ExperienceRepoSwitch.Modification.ADD_OR_UPDATE_LIST,
                                  list = listOf(nonSavedExperience.builder().isSaved(true).build()))
            BDDMockito.then(mockExperiencesRepoSwitch).should()
                    .modifyResult(ExperienceRepoSwitch.Kind.EXPLORE,
                            ExperienceRepoSwitch.Modification.UPDATE_LIST,
                            list = listOf(nonSavedExperience.builder().isSaved(true).build()))
        }

        fun should_subscribe_to_publisher_returned_by_api() {
            assertTrue(saveExperiencePublisher.hasObservers())
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
