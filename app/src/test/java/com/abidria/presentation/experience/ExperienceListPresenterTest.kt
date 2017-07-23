package com.abidria.presentation.experience

import com.abidria.data.common.Result
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
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

class ExperienceListPresenterTest {

    lateinit var presenter: ExperienceListPresenter
    @Mock lateinit var mockView: ExperienceListView
    @Mock lateinit var mockRepository: ExperienceRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
        presenter = ExperienceListPresenter(mockRepository, testSchedulerProvider)
        presenter.view = mockView
    }

    @Test
    fun testCreateAsksExperiencesAndShowsOnView() {
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        val experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        given(mockRepository.experiencesFlowable())
                .willReturn(Flowable.just(Result<List<Experience>>(arrayListOf(experienceA, experienceB), null)))

        presenter.create()

        then(mockView).should().showLoader()
        then(mockView).should().showExperienceList(arrayListOf(experienceA, experienceB))
        then(mockView).should().hideLoader()
    }

    @Test
    fun testCreateWhenResponseErrorShowsRetry() {
        given(mockRepository.experiencesFlowable())
                .willReturn(Flowable.just(Result<List<Experience>>(null, Exception())))

        presenter.create()

        then(mockView).should().hideLoader()
        then(mockView).should().showRetry()
    }

    @Test
    fun testOnRetryClickRetrieveExperiencesAndShowThem() {

        presenter.onRetryClick()

        then(mockView).should().hideRetry()
        then(mockView).should().showLoader()
        then(mockRepository).should().refreshExperiences()
    }

    @Test
    fun testExperienceTapped() {

        presenter.onExperienceClick("2")

        then(mockView).should().navigateToExperience("2")
    }

    @Test
    fun testUnsubscribenOnDestroy() {
        val testObservable = PublishSubject.create<Result<List<Experience>>>()
        assertFalse(testObservable.hasObservers())

        given(mockRepository.experiencesFlowable())
                .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))

        presenter.create()

        assertTrue(testObservable.hasObservers())

        presenter.destroy()

        assertFalse(testObservable.hasObservers())
    }
}
