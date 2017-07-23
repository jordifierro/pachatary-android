package com.abidria.data.experience

import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class ExperienceRepositoryTest {

    @Mock lateinit var mockApiRepository: ExperienceApiRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testRefreshExperiencesCallsApiRepoRefresh() {
        given(mockApiRepository.experiencesFlowable()).willReturn(Flowable.never())
        val repository = ExperienceRepository(mockApiRepository)

        repository.refreshExperiences()

        then(mockApiRepository).should().refreshExperiences()
    }

    @Test
    fun testExperiencesFlowableRecieveExperienceFromApiFlowable() {
        val testSubscriber: TestSubscriber<Result<List<Experience>>> = TestSubscriber.create()
        val experience = Experience(id = "1", title = "A", description = "", picture = null)
        val publisher: PublishSubject<Result<List<Experience>>> = PublishSubject.create()
        given(mockApiRepository.experiencesFlowable()).willReturn(publisher.toFlowable(BackpressureStrategy.LATEST))
        val repository = ExperienceRepository(mockApiRepository)

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        publisher.onNext(Result(data = Arrays.asList(experience), error = null))
        publisher.onNext(Result(data = Arrays.asList(experience), error = null))
        testSubscriber.awaitCount(2)

        val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
        val firstExperienceList = firstResult.data as List<*>
        assertEquals(experience, firstExperienceList[0])

        val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
        val secondExperienceList = secondResult.data as List<*>
        assertEquals(experience, secondExperienceList[0])
    }

    @Test
    fun testExperiencesFlowableCachesLastResult() {
        val testSubscriber: TestSubscriber<Result<List<Experience>>> = TestSubscriber.create()
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        val experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        val publisher: PublishSubject<Result<List<Experience>>> = PublishSubject.create()
        given(mockApiRepository.experiencesFlowable()).willReturn(publisher.toFlowable(BackpressureStrategy.LATEST))
        val repository = ExperienceRepository(mockApiRepository)

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        publisher.onNext(Result(data = Arrays.asList(experienceA), error = null))
        publisher.onNext(Result(data = Arrays.asList(experienceB), error = null))
        testSubscriber.awaitCount(2)

        val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
        val firstExperienceList = firstResult.data as List<*>
        assertEquals(experienceA, firstExperienceList[0])

        val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
        val secondExperienceList = secondResult.data as List<*>
        assertEquals(experienceB, secondExperienceList[0])

        val secondTestSubscriber: TestSubscriber<Result<List<Experience>>> = TestSubscriber.create()

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(secondTestSubscriber)
        secondTestSubscriber.awaitCount(1)

        val thirdResult = secondTestSubscriber.events.get(0).get(0) as Result<*>
        val thirdExperienceList = thirdResult.data as List<*>
        assertEquals(experienceB, thirdExperienceList[0])
    }

    @Test
    fun testExperienceFlowableFiltersExperienceById() {
        val testSubscriber: TestSubscriber<Result<Experience>> = TestSubscriber.create()
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        val experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        given(mockApiRepository.experiencesFlowable()).willReturn(
                Flowable.just(Result(data = Arrays.asList(experienceA, experienceB), error = null)))
        val repository = ExperienceRepository(mockApiRepository)

        repository.experienceFlowable("2").subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.await()

        val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
        val firstExperienceList = firstResult.data as Experience
        assertEquals(experienceB, firstExperienceList)
    }
}
