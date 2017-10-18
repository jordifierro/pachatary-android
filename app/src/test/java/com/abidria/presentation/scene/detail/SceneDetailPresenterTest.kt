package com.abidria.presentation.scene.detail

import com.abidria.data.common.Result
import com.abidria.data.scene.Scene
import com.abidria.data.scene.SceneRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import com.abidria.presentation.scene.detail.SceneDetailPresenter
import com.abidria.presentation.scene.detail.SceneDetailView
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

class SceneDetailPresenterTest {

    lateinit var presenter: SceneDetailPresenter
    @Mock lateinit var mockView: SceneDetailView
    @Mock lateinit var mockRepository: SceneRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
        presenter = SceneDetailPresenter(mockRepository, testSchedulerProvider)
    }

    @Test
    fun testCreateAsksSceneAndShowsIt() {
        val scene = Scene(id = "1", title = "A", description = "", picture = null,
                latitude = 0.0, longitude = 1.0, experienceId = "3")
        given(mockRepository.sceneFlowable(experienceId = "3", sceneId = "1"))
                .willReturn(Flowable.just(Result<Scene>(scene, null)))

        presenter.setView(view = mockView, experienceId = "3", sceneId = "1")
        presenter.create()

        then(mockView).should().showScene(scene)
    }

    @Test
    fun testUnsubscribenOnDestroy() {
        val testObservable = PublishSubject.create<Result<Scene>>()
        assertFalse(testObservable.hasObservers())

        given(mockRepository.sceneFlowable(experienceId = "3", sceneId = "1"))
                .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))

        presenter.setView(view = mockView, experienceId = "3", sceneId = "1")
        presenter.create()

        assertTrue(testObservable.hasObservers())

        presenter.destroy()

        assertFalse(testObservable.hasObservers())
    }
}
