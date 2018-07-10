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

class EditExperiencePresenterTest {

    @Test
    fun on_create_navigates_requests_experience() {
        given {
            an_experience()
            an_experience_repository_that_returns_experience()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_request_experience_to_the_repo()
        }
    }

    @Test
    fun on_experience_received_navigates_to_edit_title_and_description() {
        given {
            an_experience()
            an_experience_repository_that_returns_experience()
        } whenn {
            presenter_is_created()
        } then {
            should_navigate_to_edit_title_and_description_with_initial_experience_values()
        }
    }

    @Test
    fun on_title_and_description_edited_edits_experience_and_asks_user_change_picture() {
        given {
            a_title_introduced_by_user()
            a_description_introduced_by_user()
            an_experience()
            an_experience_repository_that_returns_experience()
            an_experience_repository_that_returns_edited_experience()
        } whenn {
            presenter_is_created()
            title_and_description_are_edited()
        } then {
            title_should_be_saved()
            description_should_be_saved()
            presenter_should_edit_experience_on_repo()
            presenter_should_update_received_experience()
            presenter_should_ask_user_if_wants_to_change_picture()
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
    fun on_image_selectd_presenter_should_upload_image_and_finish_view() {
        given {
            a_title_introduced_by_user()
            a_description_introduced_by_user()
            an_experience()
            an_experience_repository_that_returns_experience()
            an_experience_repository_that_returns_edited_experience()
            an_image_selected_by_user()
        } whenn {
            presenter_is_created()
            title_and_description_are_edited()
            image_is_selected()
        } then {
            presenter_should_upload_that_selected_image_with_created_experience_id()
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_select_image_canceled_presenter_should_finish() {
        given {
            nothing()
        } whenn {
            select_image_canceled()
        } then {
            presenter_should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: EditExperiencePresenter
        @Mock private lateinit var mockView: EditExperienceView
        @Mock lateinit var mockRepository: ExperienceRepository
        val experienceId = "4"
        lateinit var experience: Experience
        var userTitle = ""
        var userDescription = ""
        var image = ""
        var receivedEditedExperience: Experience? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(),
                                                          Schedulers.trampoline())
            presenter = EditExperiencePresenter(mockRepository, testSchedulerProvider)
            presenter.setView(mockView, experienceId)

            return this
        }

        fun nothing() {}

        fun a_title_introduced_by_user() {
            userTitle = "Some title"
        }

        fun a_description_introduced_by_user() {
            userDescription = "Some description"
        }

        fun an_image_selected_by_user() {
            image = "image_uri_string"
        }

        fun an_experience() {
            experience = Experience(id = experienceId, title = "Title",
                                    description = "description", picture = null)
        }

        fun an_edited_experience() {
            receivedEditedExperience = Experience(id = "1", title = userTitle,
                                                  description = userDescription, picture = null)
        }

        fun an_experience_repository_that_returns_edited_experience() {
            val sentEditedExperience = Experience(id = experienceId, title = userTitle,
                                                  description = userDescription, picture = null)
            an_edited_experience()

            BDDMockito.given(mockRepository.editExperience(experience = sentEditedExperience))
                    .willReturn(Flowable.just(ResultSuccess(receivedEditedExperience!!)))
        }

        fun an_experience_repository_that_returns_experience() {
            BDDMockito.given(mockRepository.experienceFlowable(experienceId))
                    .willReturn(Flowable.just(ResultSuccess(experience)))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun title_and_description_are_edited() {
            presenter.onTitleAndDescriptionEdited(title = userTitle, description = userDescription)
        }

        fun title_and_description_edition_canceled() {
            presenter.onEditTitleAndDescriptionCanceled()
        }

        fun image_is_selected() {
            presenter.onSelectImageSuccess(image)
        }

        fun select_image_canceled() {
            presenter.onSelectImageCancel()
        }

        fun presenter_should_edit_experience_on_repo() {
            val editedExperience = Experience(id = experienceId, title = userTitle,
                                              description = userDescription, picture = null)
            BDDMockito.then(mockRepository).should().editExperience(editedExperience)
        }

        fun presenter_should_update_received_experience() {
            Assert.assertEquals(receivedEditedExperience, presenter.experience)
        }

        fun presenter_should_ask_user_if_wants_to_change_picture() {
            BDDMockito.then(mockView).should().askUserToEditPicture()
        }

        fun title_should_be_saved() {
            Assert.assertEquals(userTitle, presenter.experience.title)
        }

        fun description_should_be_saved() {
            Assert.assertEquals(userDescription, presenter.experience.description)
        }

        fun presenter_should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun presenter_should_upload_that_selected_image_with_created_experience_id() {
            BDDMockito.then(mockRepository).should()
                    .uploadExperiencePicture(receivedEditedExperience!!.id, image)
        }

        fun presenter_should_request_experience_to_the_repo() {
            BDDMockito.then(mockRepository).should().experienceFlowable(experienceId)
        }

        fun should_navigate_to_edit_title_and_description_with_initial_experience_values() {
            BDDMockito.then(mockView).should()
                    .navigateToEditTitleAndDescription(experience.title, experience.description)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}