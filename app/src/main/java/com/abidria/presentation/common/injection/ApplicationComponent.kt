package com.abidria.presentation.common.injection

import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.experience.show.ExperienceListActivity
import com.abidria.presentation.experience.show.ExperienceMapActivity
import com.abidria.presentation.experience.edition.CreateExperienceActivity
import com.abidria.presentation.scene.edition.CreateSceneActivity
import com.abidria.presentation.scene.edition.EditSceneActivity
import com.abidria.presentation.common.view.edition.EditTitleAndDescriptionActivity
import com.abidria.presentation.common.view.edition.SelectLocationActivity
import com.abidria.presentation.experience.edition.EditExperienceActivity
import com.abidria.presentation.scene.show.SceneDetailActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, DataModule::class))
interface ApplicationComponent {

    fun inject(experienceListActivity: ExperienceListActivity)
    fun inject(experienceMapActivity: ExperienceMapActivity)
    fun inject(sceneDetailActivity: SceneDetailActivity)
    fun inject(editTitleAndDescriptionActivity: EditTitleAndDescriptionActivity)
    fun inject(selectLocationActivity: SelectLocationActivity)
    fun inject(createSceneActivity: CreateSceneActivity)
    fun inject(editSceneActivity: EditSceneActivity)
    fun inject(createExperienceActivity: CreateExperienceActivity)
    fun inject(editExperienceActivity: EditExperienceActivity)
}
