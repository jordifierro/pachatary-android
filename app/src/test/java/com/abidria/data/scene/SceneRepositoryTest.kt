package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class SceneRepositoryTest {

    @Mock lateinit var mockApiRepository: SceneApiRepository
    lateinit var repository: SceneRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        repository = SceneRepository(mockApiRepository)
    }

    @Test
    fun testGetScenesFlowableReturnsApisOne() {
        val scene = Scene(id = "9", title = "T", description = "", picture = null,
                          latitude = 1.0, longitude = 0.0, experienceId = "3")
        val scenesFlowable = Flowable.just(Result(Arrays.asList(scene), null))
        given(mockApiRepository.scenesFlowableAndRefreshObserver("3"))
                .willReturn(Pair(first = scenesFlowable, second = PublishSubject.create()))
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()

        repository.scenesFlowable("3")
                .subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedScenes = receivedResult.data as List<*>

        assertEquals(scene, receivedScenes.get(0))
    }

    @Test
    fun testGetScenesFlowableReturnsLastCached() {
        val sceneA = Scene(id = "9", title = "T", description = "", picture = null,
                           latitude = 1.0, longitude = 0.0, experienceId = "3")
        val sceneB = Scene(id = "5", title = "B", description = "", picture = null,
                           latitude = 1.0, longitude = 0.0, experienceId = "3")
        val scenesFlowable = Flowable.just(Result(Arrays.asList(sceneA), null),
                                            Result(Arrays.asList(sceneB), null))
        given(mockApiRepository.scenesFlowableAndRefreshObserver("3"))
                .willReturn(Pair(first = scenesFlowable, second = PublishSubject.create()))
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()

        repository.scenesFlowable("3")
                .subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val receivedResult = testSubscriber.events.get(0).get(1) as Result<*>
        val receivedScenes = receivedResult.data as List<*>

        assertEquals(sceneB, receivedScenes.get(0))

        val secondTestSubscriber = TestSubscriber<Result<List<Scene>>>()

        repository.scenesFlowable("3")
                .subscribeOn(Schedulers.trampoline()).subscribe(secondTestSubscriber)
        secondTestSubscriber.awaitCount(1)

        val secondReceivedResult = secondTestSubscriber.events.get(0).get(0) as Result<*>
        val secondReceivedScenes = secondReceivedResult.data as List<*>

        assertEquals(sceneB, secondReceivedScenes.get(0))
    }

    @Test
    fun testRefreshEmitsOnCachedRefresherObserver() {
        val testObserver = TestObserver<Any>()
        given(mockApiRepository.scenesFlowableAndRefreshObserver("3"))
                .willReturn(Pair(first = Flowable.never(), second = testObserver))

        repository.scenesFlowable("3")
        repository.refreshScenes("3")

        testObserver.assertValueCount(1)
    }

    @Test
    fun testGetSceneFlowableReturnsFiltered() {
        val sceneA = Scene(id = "9", title = "T", description = "", picture = null,
                           latitude = 1.0, longitude = 0.0, experienceId = "3")
        val sceneB = Scene(id = "5", title = "U", description = "", picture = null,
                latitude = 1.0, longitude = 0.0, experienceId = "3")
        val scenesFlowable = Flowable.just(Result(Arrays.asList(sceneA, sceneB), null))
        given(mockApiRepository.scenesFlowableAndRefreshObserver("3"))
                .willReturn(Pair(first = scenesFlowable, second = PublishSubject.create()))
        val testSubscriber = TestSubscriber<Result<Scene>>()

        repository.sceneFlowable(experienceId = "3", sceneId = "5")
                .subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>

        assertEquals(sceneB, receivedResult.data)
    }

    @Test
    fun testCreateSceneReturnsApiCreatedScene() {
        val sceneToCreate = Scene(id = "9", title = "T", description = "", picture = null,
                                  latitude = 1.0, longitude = 0.0, experienceId = "3")
        val resultFlowable = Flowable.never<Result<Scene>>()
        given(mockApiRepository.createScene(sceneToCreate)).willReturn(resultFlowable)

        val result = repository.createScene(sceneToCreate)

        assertEquals(resultFlowable, result)
        BDDMockito.then(mockApiRepository).should().createScene(sceneToCreate)
    }
}
