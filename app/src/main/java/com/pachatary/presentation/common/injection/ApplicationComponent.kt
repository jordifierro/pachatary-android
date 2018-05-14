package com.pachatary.presentation.common.injection

import com.pachatary.data.common.injection.DataModule
import com.pachatary.presentation.common.edition.EditTitleAndDescriptionActivity
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.experience.edition.CreateExperienceActivity
import com.pachatary.presentation.experience.edition.EditExperienceActivity
import com.pachatary.presentation.experience.show.*
import com.pachatary.presentation.login.AskLoginEmailActivity
import com.pachatary.presentation.login.LoginActivity
import com.pachatary.presentation.main.MainActivity
import com.pachatary.presentation.main.WelcomeActivity
import com.pachatary.presentation.register.ConfirmEmailActivity
import com.pachatary.presentation.register.RegisterActivity
import com.pachatary.presentation.scene.edition.CreateSceneActivity
import com.pachatary.presentation.scene.edition.EditSceneActivity
import com.pachatary.presentation.scene.show.ExperienceMapActivity
import com.pachatary.presentation.scene.show.SceneListActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, DataModule::class))
interface ApplicationComponent {

    fun inject(experienceMapActivity: ExperienceMapActivity)
    fun inject(editTitleAndDescriptionActivity: EditTitleAndDescriptionActivity)
    fun inject(selectLocationActivity: SelectLocationActivity)
    fun inject(createSceneActivity: CreateSceneActivity)
    fun inject(editSceneActivity: EditSceneActivity)
    fun inject(createExperienceActivity: CreateExperienceActivity)
    fun inject(editExperienceActivity: EditExperienceActivity)
    fun inject(exploreFragment: ExploreFragment)
    fun inject(myExperiencesFragment: MyExperiencesFragment)
    fun inject(savedFragment: SavedFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(registerActivity: RegisterActivity)
    fun inject(confirmEmailActivity: ConfirmEmailActivity)
    fun inject(sceneListActivity: SceneListActivity)
    fun inject(searchSettingsActivity: SearchSettingsActivity)
    fun inject(welcomeActivity: WelcomeActivity)
    fun inject(askLoginEmailActivity: AskLoginEmailActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(personsExperienceActivity: PersonsExperienceActivity)
}
