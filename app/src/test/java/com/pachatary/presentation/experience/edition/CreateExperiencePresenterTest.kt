package com.pachatary.presentation.experience.edition

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
            nothing()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_navigate_to_edit_title_and_description()
        }
    }

    @Test
    fun on_title_and_description_edited_saves_them_and_navigates_to_pick_image() {
        given {
            a_title()
            a_description()
            an_experience_repository_that_returns_created_experience()
        } whenn {
            title_and_description_are_edited()
        } then {
            presenter_should_create_experience()
            presenter_should_save_received_experience()
            presenter_should_navigate_to_select_image()
        }
    }

    @Test
    fun on_edit_title_and_description_canceled_presenter_should_finsh_view() {
        given {
            nothing()
        } whenn {
            title_and_description_edition_canceled()
        } then {
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_image_picked_presenter_should_navigate_to_crop_image() {
        given {
            a_title()
            a_description()
            an_experience_repository_that_returns_created_experience()
            an_image()
        } whenn {
            title_and_description_are_edited()
            image_is_selected()
        } then {
            presenter_should_upload_that_selected_image_with_created_experience_id()
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_pick_image_canceled_presenter_should_finsh_view() {
        given {
            nothing()
        } whenn {
            select_image_is_canceled()
        } then {
            presenter_should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: CreateExperiencePresenter
        @Mock private lateinit var mockView: CreateExperienceView
        @Mock lateinit var mockRepository: ExperienceRepository
        var title = ""
        var description = ""
        var image = ""
        var sentCreatedExperience: Experience? = null
        var receivedCreatedExperience: Experience? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = CreateExperiencePresenter(mockRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun a_title() {
            title = "Some title"
        }

        fun a_description() {
            description = "Some description"
        }

        fun an_image() {
            image = "image_uri_string"
        }

        fun a_created_experience() {
            receivedCreatedExperience = Experience(id = "1", title = title, description = description, picture = null)
        }

        fun an_experience_repository_that_returns_created_experience() {
            sentCreatedExperience = Experience(id = "", title = title, description = description, picture = null)
            a_created_experience()

            BDDMockito.given(mockRepository.createExperience(experience = sentCreatedExperience!!))
                    .willReturn(Flowable.just(ResultSuccess(receivedCreatedExperience!!)))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun title_and_description_are_edited() {
            presenter.onTitleAndDescriptionEdited(title = title, description = description)
        }

        fun title_and_description_edition_canceled() {
            presenter.onEditTitleAndDescriptionCanceled()
        }

        fun image_is_selected() {
            presenter.onImageSelectSuccess(image)
        }

        fun select_image_is_canceled() {
            presenter.onImageSelectCancel()
        }

        fun presenter_should_navigate_to_edit_title_and_description() {
            BDDMockito.then(mockView).should().navigateToEditTitleAndDescription()
        }

        fun presenter_should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun presenter_should_create_experience() {
            BDDMockito.then(mockRepository).should().createExperience(sentCreatedExperience!!)
        }

        fun presenter_should_save_received_experience() {
            Assert.assertEquals(receivedCreatedExperience, presenter.createdExperience)
        }

        fun presenter_should_navigate_to_select_image() {
            BDDMockito.then(mockView).should().navigateToSelectImage()
        }

        fun presenter_should_upload_that_selected_image_with_created_experience_id() {
            BDDMockito.then(mockRepository).should()
                    .uploadExperiencePicture(receivedCreatedExperience!!.id, image)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}