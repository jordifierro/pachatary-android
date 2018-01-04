package com.abidria.presentation.scene.show

import com.abidria.data.common.Result
import com.abidria.data.experience.Experience
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SceneDetailPresenterTest {

    @Test
    fun test_create_presenter_with_non_mine_experience_asks_scene_and_shows_it_and_hides_edit_button() {
        given {
            an_experience()
            an_scene()
            an_scene_repo_that_returns_that_scene()
        } whenn {
            presenter_set_view_and_create()
        } then {
            should_show_scene()
            should_hide_edit_button()
        }
    }

    @Test
    fun test_create_presenter_with_my_experience_asks_scene_and_shows_it_and_shows_edit_button() {
        given {
            a_mine_experience()
            an_scene()
            an_scene_repo_that_returns_that_scene()
        } whenn {
            presenter_set_view_and_create()
        } then {
            should_show_scene()
            should_show_edit_button()
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            an_experience()
            an_scene()
            a_test_observable()
            an_scene_repo_that_returns_that_observable()
        } whenn {
            presenter_set_view_and_create()
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
        lateinit var presenter: SceneDetailPresenter
        @Mock lateinit var mockView: SceneDetailView
        @Mock lateinit var mockRepository: SceneRepository
        lateinit var scene: Scene
        lateinit var experience: Experience
        lateinit var testObservable: PublishSubject<Result<Scene>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = SceneDetailPresenter(mockRepository, testSchedulerProvider)

            return this
        }

        fun an_experience() {
            experience = Experience(id = "1", title = "A", description = "", picture = null, isMine = false)
        }

        fun a_mine_experience() {
            experience = Experience(id = "1", title = "A", description = "", picture = null, isMine = true)
        }

        fun an_scene() {
            scene = Scene(id = "1", title = "A", description = "", picture = null,
                          latitude = 0.0, longitude = 1.0, experienceId = experience.id)
        }

        fun an_scene_repo_that_returns_that_scene() {
            given(mockRepository.sceneFlowable(experienceId = experience.id, sceneId = scene.id))
                    .willReturn(Flowable.just(Result<Scene>(scene, null)))
        }

        fun a_test_observable() {
            testObservable = PublishSubject.create<Result<Scene>>()
            assertFalse(testObservable.hasObservers())
        }

        fun an_scene_repo_that_returns_that_observable() {
            given(mockRepository.sceneFlowable(experienceId = experience.id, sceneId = scene.id))
                    .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun presenter_set_view_and_create() {
            presenter.setView(view = mockView, experienceId = experience.id,
                              sceneId = scene.id, isMine = experience.isMine)
            presenter.create()
        }

        fun presenter_destroy() {
            presenter.destroy()
        }

        fun should_show_scene() {
            BDDMockito.then(mockView).should().showScene(scene)
        }

        fun observable_should_have_observers() {
            assertTrue(testObservable.hasObservers())
        }

        fun observable_should_have_no_observers() {
            assertFalse(testObservable.hasObservers())
        }

        fun should_show_edit_button() {
            BDDMockito.then(mockView).should().showEditButton()
        }

        fun should_hide_edit_button() {
            BDDMockito.then(mockView).should().hideEditButton()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
