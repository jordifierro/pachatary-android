package com.abidria.presentation.experience

import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

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
        given(mockRepository.getScenes(experienceId = "5")).willReturn(Flowable.just(arrayListOf(sceneA, sceneB)))
        given(mockView.mapLoadedFlowable()).willReturn(Flowable.just(true))
        given(mockExperienceRepository.getExperience("5")).willReturn(Flowable.never())

        presenter.create()

        then(mockView).should().showScenesOnMap(arrayListOf(sceneA, sceneB))
    }

    @Test
    fun testCreateGetsExperienceAndSetsTitle() {
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        given(mockRepository.getScenes("5")).willReturn(Flowable.never())
        given(mockView.mapLoadedFlowable()).willReturn(Flowable.never())
        given(mockExperienceRepository.getExperience(experienceId = "5")).willReturn(Flowable.just(experienceA))

        presenter.create()

        then(mockView).should().setTitle(title = "A")
    }

    @Test
    fun testNavigatesToSceneOnSceneClick() {

        presenter.onSceneClick(sceneId = "8")

        then(mockView).should().navigateToScene(experienceId = "5", sceneId = "8")
    }
}
