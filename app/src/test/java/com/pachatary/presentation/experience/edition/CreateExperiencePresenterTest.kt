package com.pachatary.presentation.experience.edition

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

class CreateExperiencePresenterTest {

    @Test
    fun test_empty_title() {
        given {
            a_title("")
        } whenn {
            create_click()
        } then {
            should_show_title_error()
        }
    }

    @Test
    fun test_empty_description() {
        given {
            a_title("title")
            a_description("")
        } whenn {
            create_click()
        } then {
            should_show_description_error()
        }
    }

    @Test
    fun test_empty_picture() {
        given {
            a_title("title")
            a_description("desc")
            a_picture(null)
        } whenn {
            create_click()
        } then {
            should_show_picture_error()
        }
    }

    @Test
    fun test_inprogress() {
        given {
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("", "title", "desc", null),
                        returns = ResultInProgress())
        } whenn {
            create_click()
        } then {
            should_show_loader()
            should_disable_button()
        }
    }

    @Test
    fun test_error() {
        given {
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("", "title", "desc", null),
                    returns = DummyResultError())
        } whenn {
            create_click()
        } then {
            should_hide_loader()
            should_enable_button()
            should_show_error()
        }
    }

    @Test
    fun test_success() {
        given {
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_repo_that(forParams = Experience("", "title", "desc", null),
                    returns = DummyExperienceResultSuccess("4"))
        } whenn {
            create_click()
        } then {
            should_hide_loader()
            should_call_upload_picture("4", "pic")
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: CreateExperiencePresenter
        @Mock private lateinit var mockView: CreateExperienceView
        @Mock private lateinit var mockRepository: ExperienceRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = CreateExperiencePresenter(mockRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
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
            BDDMockito.given(mockRepository.createExperience(forParams))
                    .willReturn(Flowable.just(returns))
        }

        fun create_click() {
            presenter.onCreateButtonClick()
        }

        fun should_show_title_error() {
            BDDMockito.then(mockView).should().showTitleError()
        }

        fun should_show_description_error() {
            BDDMockito.then(mockView).should().showDescriptionError()
        }

        fun should_show_picture_error() {
            BDDMockito.then(mockView).should().showPictureError()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_button() {
            BDDMockito.then(mockView).should().disableCreateButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_enable_button() {
            BDDMockito.then(mockView).should().enableCreateButton()
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