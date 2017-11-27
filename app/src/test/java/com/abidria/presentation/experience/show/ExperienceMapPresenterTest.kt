package com.abidria.presentation.experience.show

import com.abidria.data.common.Result
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
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
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class ExperienceMapPresenterTest {

    @Test
    fun testCreateWaitsMapAndScenesAndShowsThem() {
        given {
            two_scenes()
            repository_with_those_two_scenes()
            map_is_loaded_correctly()
            experience_repo_with_no_experiences()
        } whenn {
            presenter_is_created()
        } then {
            view_should_show_loader()
            view_should_hide_loader()
            view_should_show_scenes_on_map()
        }
    }

    @Test
    fun testCreateGetsExperienceAndSetsTitle() {
        given {
            an_experience()
            experience_repo_with_that_experience()
            repository_with_no_scenes()
            map_not_loaded()
        } whenn {
            presenter_is_created()
        } then {
            view_should_show_experience_title()
        }
    }

    @Test
    fun testNavigatesToSceneOnSceneClick() {
        given {
            an_experience()
            an_scene()
        } whenn {
            this_scene_is_clicked()
        } then {
            view_should_navigate_to_that_scene_with_experience_id()
        }
    }

    @Test
    fun testNavigatesToCreateSceneClick() {
        given {
            nothing()
        } whenn {
            create_scene_button_is_clicked()
        } then {
            view_should_navigate_to_create_scene_with_experience_id()
        }
    }

    @Test
    fun testUnsubscribenOnDestroy() {
        given {
            scenes_experiences_and_map_observables()
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

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: ExperienceMapPresenter
        @Mock private lateinit var mockView: ExperienceMapView
        @Mock lateinit var mockRepository: SceneRepository
        @Mock lateinit var mockExperienceRepository: ExperienceRepository
        val experienceId = "5"
        var sceneA: Scene? = null
        var sceneB: Scene? = null
        var experienceA: Experience? = null
        var scenesObservable = PublishSubject.create<Result<List<Scene>>>()
        var experienceObservable = PublishSubject.create<Result<Experience>>()
        var mapObservable = PublishSubject.create<Any>()

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = ExperienceMapPresenter(repository = mockRepository, experienceRepository = mockExperienceRepository,
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
                    .willReturn(Flowable.just(Result(Arrays.asList(sceneA!!, sceneB!!), null)))
        }

        fun repository_with_no_scenes() {
            BDDMockito.given(mockRepository.scenesFlowable("5")).willReturn(Flowable.never())
        }

        fun map_is_loaded_correctly() {
            BDDMockito.given(mockView.mapLoadedFlowable()).willReturn(Flowable.just(true))
        }

        fun map_not_loaded() {
            BDDMockito.given(mockView.mapLoadedFlowable()).willReturn(Flowable.just(false))
        }

        fun an_experience() {
            experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        }

        fun experience_repo_with_that_experience() {
            BDDMockito.given(mockExperienceRepository.experienceFlowable(experienceId = "5"))
                    .willReturn(Flowable.just(Result(experienceA, null)))
        }

        fun experience_repo_with_no_experiences() {
            BDDMockito.given(mockExperienceRepository.experienceFlowable("5")).willReturn(Flowable.never())
        }

        fun scenes_experiences_and_map_observables() {
            BDDMockito.given(mockRepository.scenesFlowable(experienceId = experienceId))
                    .willReturn(scenesObservable.toFlowable(BackpressureStrategy.LATEST))
            BDDMockito.given(mockExperienceRepository.experienceFlowable(experienceId = experienceId))
                    .willReturn(experienceObservable.toFlowable(BackpressureStrategy.LATEST))
            BDDMockito.given(mockView.mapLoadedFlowable()).willReturn(mapObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun presenter_is_destroyed() {
            presenter.destroy()
        }

        fun this_scene_is_clicked() {
            presenter.onSceneClick(sceneId = sceneA!!.id)
        }

        fun create_scene_button_is_clicked() {
            presenter.onCreateSceneClick()
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

        fun view_should_show_experience_title() {
            BDDMockito.then(mockView).should().setTitle(title = experienceA!!.title)
        }

        fun view_should_navigate_to_that_scene_with_experience_id() {
            BDDMockito.then(mockView).should().navigateToScene(experienceId = experienceId, sceneId = sceneA!!.id)
        }

        fun view_should_navigate_to_create_scene_with_experience_id() {
            BDDMockito.then(mockView).should().navigateToCreateScene(experienceId = experienceId)
        }

        fun those_observables_should_have_observers() {
            assertTrue(scenesObservable.hasObservers())
            assertTrue(experienceObservable.hasObservers())
            assertTrue(mapObservable.hasObservers())
        }

        fun those_observables_should_have_no_observers() {
            assertFalse(scenesObservable.hasObservers())
            assertFalse(experienceObservable.hasObservers())
            assertFalse(mapObservable.hasObservers())
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
