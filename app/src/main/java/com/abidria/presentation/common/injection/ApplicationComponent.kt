package com.abidria.presentation.common.injection

import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.experience.ExperienceListActivity
import com.abidria.presentation.experience.ExperienceMapActivity
import com.abidria.presentation.experience.create.CreateExperienceActivity
import com.abidria.presentation.scene.create.CreateSceneActivity
import com.abidria.presentation.scene.create.EditSceneActivity
import com.abidria.presentation.scene.create.EditTitleAndDescriptionActivity
import com.abidria.presentation.scene.create.SelectLocationActivity
import com.abidria.presentation.scene.detail.SceneDetailActivity
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
}
