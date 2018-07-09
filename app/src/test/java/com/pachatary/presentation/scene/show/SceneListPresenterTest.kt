package com.pachatary.presentation.scene.show

import com.pachatary.data.*
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SceneListPresenterTest {

    enum class SceneListPresenterAction {
        CREATE, RETRY
    }

    @Test
    fun test_experience_response_error() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(DummyResultError(), forExperienceId = "4")
                an_scene_repo_that_returns(forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_retry()
            }
        }
    }

    @Test
    fun test_experience_response_inprogress() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(ResultInProgress(), forExperienceId = "4")
                an_scene_repo_that_returns(forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_loading_experience()
            }
        }
    }

    @Test
    fun test_experience_response_success() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(ExperienceResultSuccess("4"), forExperienceId = "4")
                an_scene_repo_that_returns(forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_experience(DummyExperience("4"))
            }
        }
    }

    @Test
    fun test_scene_response_error() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(forExperienceId = "4")
                an_scene_repo_that_returns(DummyResultError(), forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_retry()
            }
        }
    }

    @Test
    fun test_scene_response_inprogress() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(forExperienceId = "4")
                an_scene_repo_that_returns(ResultInProgress(), forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_loading_scenes()
            }
        }
    }

    @Test
    fun test_scene_response_success() {
        for (action in SceneListPresenterAction.values()) {
            given {
                an_experience_repo_that_returns(forExperienceId = "4")
                an_scene_repo_that_returns(ScenesListResultSuccess("6", "7"), forExperienceId = "4")
                a_presenter_with(experienceId = "4")
            } whenn {
                presenter_action(action)
            } then {
                should_show_scenes(listOf(DummyScene("6"), DummyScene("7")))
            }
        }
    }

    @Test
    fun test_save() {
        given {
            a_presenter_with(experienceId = "4")
        } whenn {
            save_experience(true)
        } then {
            should_call_repo_save("4", true)
            should_show_saved_message()
        }
    }

    @Test
    fun test_unsave() {
        given {
            a_presenter_with(experienceId = "4")
        } whenn {
            save_experience(false)
        } then {
            should_ask_are_you_sure()
        }
    }

    @Test
    fun test_unsave_confirmation() {
        given {
            a_presenter_with(experienceId = "4")
        } whenn {
            unsave_confirmation()
        } then {
            should_call_repo_save("4", false)
        }
    }

    @Test
    fun test_edit_experience_click() {
        given {
            a_presenter_with(experienceId = "9")
        } whenn {
            edit_experience_click()
        } then {
            should_navigate_to_edit_experience("9")
        }
    }

    @Test
    fun test_presenter_navigates_to_edit_scene() {
        given {
            a_presenter_with(experienceId = "4")
        } whenn {
            edit_scene_click("7")
        } then {
            should_navigate_to_edit_scene("7", "4")
        }
    }

    @Test
    fun test_on_map_button_click() {
        given {
            a_presenter_with(experienceId = "5")
        } whenn {
            map_button_click()
        } then {
            should_navigate_to_map("5")
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: SceneListPresenter
        @Mock lateinit var mockView: SceneListView
        @Mock lateinit var mockRepository: SceneRepository
        @Mock lateinit var mockExperienceRepository: ExperienceRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = SceneListPresenter(mockRepository, mockExperienceRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun a_presenter_with(experienceId: String) {
            presenter.experienceId = experienceId
        }

        fun an_experience_repo_that_returns(vararg results: Result<Experience>,
                                            forExperienceId: String) {
            BDDMockito.given(mockExperienceRepository.experienceFlowable(forExperienceId))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun an_scene_repo_that_returns(vararg results: Result<List<Scene>>,
                                       forExperienceId: String) {
            BDDMockito.given(mockRepository.scenesFlowable(forExperienceId))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun presenter_action(action: SceneListPresenterAction) {
            when (action) {
                SceneListPresenterAction.CREATE -> presenter.create()
                SceneListPresenterAction.RETRY -> presenter.onRetryClick()
            }
        }

        fun edit_scene_click(sceneId: String) {
            presenter.onEditSceneClick(sceneId)
        }

        fun edit_experience_click() {
            presenter.onEditExperienceClick()
        }

        fun map_button_click() {
            presenter.onMapButtonClick()
        }

        fun save_experience(save: Boolean) {
            presenter.onExperienceSave(save)
        }

        fun should_navigate_to_edit_experience(experienceId: String) {
            BDDMockito.then(mockView).should().navigateToEditExperience(experienceId)
        }

        fun should_navigate_to_edit_scene(sceneId: String, experienceId: String) {
            BDDMockito.then(mockView).should().navigateToEditScene(sceneId, experienceId)
        }

        fun should_show_retry() {
            BDDMockito.then(mockView).should().showRetry()
        }

        fun should_show_loading_experience() {
            BDDMockito.then(mockView).should().showLoadingExperience()
        }

        fun should_show_experience(experience: Experience) {
            BDDMockito.then(mockView).should().showExperience(experience)
        }

        fun should_show_loading_scenes() {
            BDDMockito.then(mockView).should().showLoadingScenes()
        }

        fun should_show_scenes(scenes: List<Scene>) {
            BDDMockito.then(mockView).should().showScenes(scenes)
        }

        fun should_navigate_to_map(experienceId: String) {
            BDDMockito.then(mockView).should().navigateToExperienceMap(experienceId)
        }

        fun should_call_repo_save(experienceId: String, save: Boolean) {
            BDDMockito.then(mockExperienceRepository).should().saveExperience(experienceId, save)
        }

        fun should_show_saved_message() {
            BDDMockito.then(mockView).should().showSavedMessage()
        }

        fun should_ask_are_you_sure() {
            BDDMockito.then(mockView).should().showUnsaveDialog()
        }

        fun unsave_confirmation() {
            presenter.onConfirmUnsaveExperience()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
