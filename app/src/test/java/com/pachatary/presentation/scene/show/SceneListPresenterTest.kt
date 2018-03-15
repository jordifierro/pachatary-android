package com.pachatary.presentation.scene.show

import com.pachatary.data.common.Result
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

    @Test
    fun test_presenter_loads_experience_and_scenes_and_show_them_scrolling_to_selected_one() {
        given {
            an_experience_id()
            an_scene_id()
            an_experience()
            an_scene()
            an_scene_repo_that_returns_that_scene()
            an_experience_repo_that_returns_that_experience()
        } whenn {
            presenter_set_view_and_create_with_experience_and_scene_id()
        } then {
            should_call_scene_repo_get_scenes_with_experience_id()
            should_call_experience_repo_with_experience_id()
            should_show_experience_scenes_and_scroll_to_scene()
        }
    }

    @Test
    fun test_presenter_navigates_to_edit_experience() {
        given {
            an_experience_id()
            an_scene_id()
            an_experience()
            an_scene()
            an_scene_repo_that_returns_that_scene()
            an_experience_repo_that_returns_that_experience()
        } whenn {
            presenter_set_view_and_create_with_experience_and_scene_id()
            presenter_on_edit_experience_click("99")
        } then {
            should_navigate_to_edit_experience("99")
        }
    }

    @Test
    fun test_presenter_navigates_to_edit_scene() {
        given {
            an_experience_id()
            an_scene_id()
            an_experience()
            an_scene()
            an_scene_repo_that_returns_that_scene()
            an_experience_repo_that_returns_that_experience()
        } whenn {
            presenter_set_view_and_create_with_experience_and_scene_id()
            presenter_on_edit_scene_click("76")
        } then {
            should_navigate_to_edit_scene("76")
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            an_experience_id()
            an_scene_id()
            an_experience()
            an_scene()
            a_test_scenes_observable()
            an_scene_repo_that_returns_that_observable()
            a_test_experience_observable()
            an_experience_repo_that_returns_that_observable()
        } whenn {
            presenter_set_view_and_create_with_experience_and_scene_id()
        } then {
            observable_should_have_observers()
        } whenn {
            presenter_destroy()
        } then {
            observable_should_have_no_observers()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: SceneListPresenter
        @Mock lateinit var mockView: SceneListView
        @Mock lateinit var mockRepository: SceneRepository
        @Mock lateinit var mockExperienceRepository: ExperienceRepository
        lateinit var sceneId: String
        lateinit var experienceId: String
        lateinit var scene: Scene
        lateinit var experience: Experience
        lateinit var testScenesObservable: PublishSubject<Result<List<Scene>>>
        lateinit var testExperienceObservable: PublishSubject<Result<Experience>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = SceneListPresenter(mockRepository, mockExperienceRepository, testSchedulerProvider)

            return this
        }

        fun an_experience_id() {
            experienceId = "8"
        }

        fun an_scene_id() {
            sceneId = "5"
        }

        fun an_experience() {
            experience = Experience(id = "1", title = "A", description = "", picture = null, isMine = false)
        }

        fun an_scene() {
            scene = Scene(id = "1", title = "A", description = "", picture = null,
                          latitude = 0.0, longitude = 1.0, experienceId = experience.id)
        }

        fun a_test_scenes_observable() {
            testScenesObservable = PublishSubject.create<Result<List<Scene>>>()
            assertFalse(testScenesObservable.hasObservers())
        }

        fun a_test_experience_observable() {
            testExperienceObservable = PublishSubject.create<Result<Experience>>()
            assertFalse(testExperienceObservable.hasObservers())
        }

        fun an_scene_repo_that_returns_that_observable() {
            given(mockRepository.scenesFlowable(experienceId = experienceId))
                    .willReturn(testScenesObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun an_experience_repo_that_returns_that_observable() {
            given(mockExperienceRepository.experienceFlowable(experienceId = experienceId))
                    .willReturn(testExperienceObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun an_scene_repo_that_returns_that_scene() {
            given(mockRepository.scenesFlowable(experienceId = experienceId))
                    .willReturn(Flowable.just(Result<List<Scene>>(arrayListOf(scene), null)))
        }

        fun an_experience_repo_that_returns_that_experience() {
            given(mockExperienceRepository.experienceFlowable(experienceId = experienceId))
                    .willReturn(Flowable.just(Result(experience, null)))
        }

        fun presenter_set_view_and_create_with_experience_and_scene_id() {
            presenter.setView(view = mockView, experienceId = experienceId,
                              selectedSceneId = sceneId, isMine = experience.isMine)
            presenter.create()
        }

        fun presenter_destroy() {
            presenter.destroy()
        }

        fun presenter_on_edit_experience_click(experienceId: String) {
            presenter.onEditExperienceClick(experienceId)
        }

        fun presenter_on_edit_scene_click(sceneId: String) {
            presenter.onEditSceneClick(sceneId)
        }

        fun should_call_scene_repo_get_scenes_with_experience_id() {
            BDDMockito.then(mockRepository).should().scenesFlowable(this.experienceId)
        }

        fun should_call_experience_repo_with_experience_id() {
            BDDMockito.then(mockExperienceRepository).should().experienceFlowable(this.experienceId)
        }

        fun should_show_experience_scenes_and_scroll_to_scene() {
            BDDMockito.then(mockView).should()
                    .showExperienceScenesAndScrollToSelectedIfFirstTime(experience, arrayListOf(scene), this.sceneId)
        }

        fun observable_should_have_observers() {
            assertTrue(testScenesObservable.hasObservers())
            assertTrue(testExperienceObservable.hasObservers())
        }

        fun observable_should_have_no_observers() {
            assertFalse(testScenesObservable.hasObservers())
            assertFalse(testExperienceObservable.hasObservers())
        }

        fun should_navigate_to_edit_experience(experienceId: String) {
            BDDMockito.then(mockView).should().navigateToEditExperience(experienceId)
        }

        fun should_navigate_to_edit_scene(sceneId: String) {
            BDDMockito.then(mockView).should().navigateToEditScene(sceneId, this.experienceId)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
