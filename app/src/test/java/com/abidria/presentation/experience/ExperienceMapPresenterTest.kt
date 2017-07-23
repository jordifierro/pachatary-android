package com.abidria.presentation.experience

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
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class ExperienceMapPresenterTest {

    lateinit var presenter: ExperienceMapPresenter
    @Mock lateinit var mockView: ExperienceMapView
    @Mock lateinit var mockRepository: SceneRepository
    @Mock lateinit var mockExperienceRepository: ExperienceRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
        presenter = ExperienceMapPresenter(repository = mockRepository, experienceRepository = mockExperienceRepository,
                                           schedulerProvider = testSchedulerProvider)
        presenter.setView(view = mockView, experienceId = "5")
    }

    @Test
    fun testCreateWaitsMapAndScenesAndShowsThem() {
        val sceneA = Scene(id = "1", title = "A", description = "", picture = null,
                latitude = 0.0, longitude = 0.0, experienceId = "5")
        val sceneB = Scene(id = "2", title = "B", description = "", picture = null,
                latitude = 0.0, longitude = 0.0, experienceId = "5")
        given(mockRepository.scenesFlowable(experienceId = "5"))
                .willReturn(Flowable.just(Result(Arrays.asList(sceneA, sceneB), null)))
        given(mockView.mapLoadedFlowable()).willReturn(Flowable.just(true))
        given(mockExperienceRepository.experienceFlowable("5")).willReturn(Flowable.never())

        presenter.create()

        then(mockView).should().showLoader()
        then(mockView).should().hideLoader()
        then(mockView).should().showScenesOnMap(arrayListOf(sceneA, sceneB))
    }

    @Test
    fun testCreateGetsExperienceAndSetsTitle() {
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        given(mockRepository.scenesFlowable("5")).willReturn(Flowable.never())
        given(mockView.mapLoadedFlowable()).willReturn(Flowable.never())
        given(mockExperienceRepository.experienceFlowable(experienceId = "5"))
                .willReturn(Flowable.just(Result(experienceA, null)))

        presenter.create()

        then(mockView).should().setTitle(title = "A")
    }

    @Test
    fun testNavigatesToSceneOnSceneClick() {

        presenter.onSceneClick(sceneId = "8")

        then(mockView).should().navigateToScene(experienceId = "5", sceneId = "8")
    }

    @Test
    fun testUnsubscribenOnDestroy() {
        val scenesObservable = PublishSubject.create<Result<List<Scene>>>()
        val experienceObservable = PublishSubject.create<Result<Experience>>()
        val mapObservable = PublishSubject.create<Any>()
        assertFalse(scenesObservable.hasObservers())
        assertFalse(experienceObservable.hasObservers())
        assertFalse(mapObservable.hasObservers())

        given(mockRepository.scenesFlowable(experienceId = "3"))
                .willReturn(scenesObservable.toFlowable(BackpressureStrategy.LATEST))
        given(mockExperienceRepository.experienceFlowable("3"))
                .willReturn(experienceObservable.toFlowable(BackpressureStrategy.LATEST))
        given(mockView.mapLoadedFlowable()).willReturn(mapObservable.toFlowable(BackpressureStrategy.LATEST))

        presenter.setView(view = mockView, experienceId = "3")
        presenter.create()

        assertTrue(scenesObservable.hasObservers())
        assertTrue(experienceObservable.hasObservers())
        assertTrue(mapObservable.hasObservers())

        presenter.destroy()

        assertFalse(scenesObservable.hasObservers())
        assertFalse(experienceObservable.hasObservers())
        assertFalse(mapObservable.hasObservers())
    }
}
