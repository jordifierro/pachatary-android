package com.pachatary.presentation.scene.show

import com.pachatary.data.common.Result
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ExperienceMapPresenterTest {

    @Test
    fun testCreateWaitsMapAndScenesAndShowsThem() {
        given {
            two_scenes()
            repository_with_those_two_scenes()
            map_is_loaded_correctly()
        } whenn {
            presenter_is_created()
        } then {
            view_should_show_loader()
            view_should_hide_loader()
            view_should_show_scenes_on_map()
        }
    }

    @Test
    fun testUnsubscribenOnDestroy() {
        given {
            scenes_and_map_observables()
        } then {
            those_observables_should_have_no_observers()
        } whenn {
            presenter_is_created()
        } then {
            those_observables_should_have_observers()
        } whenn {
            presenter_is_destroyed()
        } then {
            those_observables_should_have_no_observers()
        }
    }

    @Test
    fun test_on_scene_click_finishes_with_scene_id() {
        given {

        } whenn {
            scene_click("8")
        } then {
            should_finish_with_scene_id("8")
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: ExperienceMapPresenter
        @Mock private lateinit var mockView: ExperienceMapView
        @Mock lateinit var mockRepository: SceneRepository
        val experienceId = "5"
        var sceneA: Scene? = null
        var sceneB: Scene? = null
        var scenesObservable = PublishSubject.create<Result<List<Scene>>>()
        var mapObservable = PublishSubject.create<Any>()

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(),
                                                          Schedulers.trampoline())
            presenter = ExperienceMapPresenter(repository = mockRepository,
                                               schedulerProvider = testSchedulerProvider)
            presenter.setView(view = mockView, experienceId = experienceId)

            return this
        }

        fun nothing() {}

        fun an_scene() {
            sceneA = Scene(id = "1", title = "A", description = "", picture = null,
                    latitude = 0.0, longitude = 0.0, experienceId = "5")
        }

        fun two_scenes() {
            an_scene()
            sceneB = Scene(id = "2", title = "B", description = "", picture = null,
                    latitude = 0.0, longitude = 0.0, experienceId = "5")
        }

        fun repository_with_those_two_scenes() {
            BDDMockito.given(mockRepository.scenesFlowable(experienceId = "5"))
                    .willReturn(Flowable.just(ResultSuccess(listOf(sceneA!!, sceneB!!))))
        }

        fun map_is_loaded_correctly() {
            BDDMockito.given(mockView.mapLoadedFlowable()).willReturn(Flowable.just(true))
        }

        fun scenes_and_map_observables() {
            BDDMockito.given(mockRepository.scenesFlowable(experienceId = experienceId))
                    .willReturn(scenesObservable.toFlowable(BackpressureStrategy.LATEST))
            BDDMockito.given(mockView.mapLoadedFlowable()).willReturn(mapObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun presenter_is_destroyed() {
            presenter.destroy()
        }

        fun scene_click(sceneId: String) {
            presenter.onSceneClick(sceneId)
        }

        fun view_should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun view_should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun view_should_show_scenes_on_map() {
            BDDMockito.then(mockView).should().showScenesOnMap(arrayListOf(sceneA!!, sceneB!!))
        }

        fun those_observables_should_have_observers() {
            assertTrue(scenesObservable.hasObservers())
            assertTrue(mapObservable.hasObservers())
        }

        fun those_observables_should_have_no_observers() {
            assertFalse(scenesObservable.hasObservers())
            assertFalse(mapObservable.hasObservers())
        }

        fun should_finish_with_scene_id(sceneId: String) {
            BDDMockito.then(mockView).should().finishWithSceneId(sceneId)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
