package com.pachatary.presentation.experience.edition

import com.pachatary.data.DummyExperience
import com.pachatary.data.ExperienceResultSuccess
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CreateExperiencePresenterTest {

    @Test
    fun on_create_navigates_to_edit_title_and_description() {
        given {

        } whenn {
            presenter_is_created()
        } then {
            should_navigate_to_edit_title_and_description()
        }
    }

    @Test
    fun on_title_and_description_and_select_image_creates_experience_uploads_image_and_finishes() {
        given {
            an_experience_repository_that_returns_created_experience_with_id("3", "ttt", "ddd")
        } whenn {
            title_and_description_are_edited("ttt", "ddd")
            image_is_selected("picture_path")
        } then {
            should_create_experience("ttt", "ddd")
            should_upload_picture("3", "picture_path")
            should_finish_view()
        }
    }

    @Test
    fun on_edit_title_and_description_canceled_presenter_should_finsh_view() {
        given {

        } whenn {
            title_and_description_edition_canceled()
        } then {
            should_finish_view()
        }
    }

    @Test
    fun on_select_image_canceled_presenter_should_finsh_view() {
        given {

        } whenn {
            title_and_description_are_edited("t", "s")
            select_image_is_canceled()
        } then {
            should_navigate_to_edit_title_and_description("t", "s")
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

        fun an_experience_repository_that_returns_created_experience_with_id(id: String,
                                                                             title: String,
                                                                             description: String) {
            BDDMockito.given(mockRepository.createExperience(
                    Experience(id = "", title = title, description = description, picture = null)))
                    .willReturn(Flowable.just(ExperienceResultSuccess(id)))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun title_and_description_are_edited(title: String, description: String) {
            presenter.onTitleAndDescriptionEdited(title = title, description = description)
        }

        fun title_and_description_edition_canceled() {
            presenter.onEditTitleAndDescriptionCanceled()
        }

        fun image_is_selected(imageString: String) {
            presenter.onImageSelectSuccess(imageString)
        }

        fun select_image_is_canceled() {
            presenter.onImageSelectCancel()
        }

        fun should_navigate_to_edit_title_and_description(title: String? = null,
                                                          description: String? = null) {
            if (title == null)
                BDDMockito.then(mockView).should().navigateToEditTitleAndDescription()
            else
                BDDMockito.then(mockView).should()
                        .navigateToEditTitleAndDescription(title, description!!)
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun should_create_experience(title: String, description: String) {
            BDDMockito.then(mockRepository).should().createExperience(
                    Experience(id = "", title = title, description = description, picture = null))
        }

        fun should_upload_picture(id: String, imageString: String) {
            BDDMockito.then(mockRepository).should().uploadExperiencePicture(id, imageString)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}