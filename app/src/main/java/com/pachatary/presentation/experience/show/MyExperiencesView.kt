package com.pachatary.presentation.experience.show

import android.content.Context
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.view.LoaderView

interface MyExperiencesView {

    fun showExperienceList(experiences: List<Experience>)
    fun showExperiencesRetry()
    fun hideExperiencesRetry()
    fun showExperiencesLoader()
    fun hideExperiencesLoader()
    fun showPaginationLoader()
    fun hidePaginationLoader()

    fun showProfile(profile: Profile)
    fun hideProfileLoader()
    fun hideProfileRetry()
    fun showProfileLoader()
    fun showProfileRetry()

    fun navigateToExperience(experienceId: String)
    fun navigateToCreateExperience()
    fun navigateToRegister()
    fun navigateToPickAndCropImage()
}
