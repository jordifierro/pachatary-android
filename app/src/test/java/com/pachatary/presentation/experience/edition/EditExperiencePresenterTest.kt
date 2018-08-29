package com.pachatary.presentation.experience.edition

import com.pachatary.data.DummyExperience
import com.pachatary.data.DummyExperienceResultSuccess
import com.pachatary.data.DummyResultError
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class EditExperiencePresenterTest {

    @Test
    fun test_empty_title() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("")
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_show_title_error()
        }
    }

    @Test
    fun test_empty_description() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("title")
            a_description("")
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_show_description_error()
        }
    }

    @Test
    fun test_inprogress() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("9", "title", "desc"),
                        returns = ResultInProgress())
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_show_loader()
            should_disable_button()
        }
    }

    @Test
    fun test_error() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("9", "title", "desc"),
                        returns = DummyResultError())
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_hide_loader()
            should_enable_button()
            should_show_error()
        }
    }

    @Test
    fun test_success_without_picture() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("title")
            a_description("desc")
            a_picture(null)
            a_repo_that(forParams = Experience("9", "title", "desc"),
                        returns = DummyExperienceResultSuccess("4"))
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_hide_loader()
            should_finish_view()
        }
    }

    @Test
    fun test_success_with_picture() {
        given {
            an_experience_id("4")
            a_repo_that_on_get_experience(forId = "4", returns = DummyExperienceResultSuccess("9"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("9", "title", "desc"),
                        returns = DummyExperienceResultSuccess("4"))
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_experience(DummyExperience("9"))
            should_hide_loader()
            should_call_upload_picture("4", "pic")
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: EditExperiencePresenter
        @Mock private lateinit var mockView: EditExperienceView
        @Mock private lateinit var mockRepository: ExperienceRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = EditExperiencePresenter(mockRepository, testSchedulerProvider)

            return this
        }

        fun an_experience_id(id: String) {
            presenter.setViewAndExperienceId(mockView, id)
        }

        fun a_title(title: String) {
            BDDMockito.given(mockView.title()).willReturn(title)
        }

        fun a_description(description: String) {
            BDDMockito.given(mockView.description()).willReturn(description)
        }

        fun a_picture(picture: String?) {
            BDDMockito.given(mockView.picture()).willReturn(picture)
        }

        fun a_repo_that(forParams: Experience, returns: Result<Experience>) {
            BDDMockito.given(mockRepository.editExperience(forParams))
                    .willReturn(Flowable.just(returns))
        }

        fun a_repo_that_on_get_experience(forId: String, returns: Result<Experience>) {
            BDDMockito.given(mockRepository.experienceFlowable(forId))
                    .willReturn(Flowable.just(returns))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun update_click() {
            presenter.onUpdateButtonClick()
        }

        fun should_show_experience(experience: Experience) {
            BDDMockito.then(mockView).should().showExperience(experience)
        }

        fun should_show_title_error() {
            BDDMockito.then(mockView).should().showTitleError()
        }

        fun should_show_description_error() {
            BDDMockito.then(mockView).should().showDescriptionError()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_button() {
            BDDMockito.then(mockView).should().disableUpdateButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_enable_button() {
            BDDMockito.then(mockView).should().enableUpdateButton()
        }

        fun should_show_error() {
            BDDMockito.then(mockView).should().showError()
        }

        fun should_call_upload_picture(experienceId: String, picture: String) {
            BDDMockito.then(mockRepository).should().uploadExperiencePicture(experienceId, picture)
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
