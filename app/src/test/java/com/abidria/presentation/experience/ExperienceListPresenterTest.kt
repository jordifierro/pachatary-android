package com.abidria.presentation.experience

import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
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
        presenter.setView(mockView)
    }

    @Test
    fun testCreateAsksExperiencesAndShowsOnView() {
        val experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        val experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        given(mockRepository.getExperiences()).willReturn(Flowable.just(arrayListOf(experienceA, experienceB)))

        presenter.create()

        then(mockView).should().showExperienceList(arrayListOf(experienceA, experienceB))
    }

    @Test
    fun testExperienceTapped() {

        presenter.onExperienceClick("2")

        then(mockView).should().navigateToExperience("2")
    }
}
