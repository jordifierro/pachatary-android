package com.abidria.presentation.common

import android.app.Application
import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.common.injection.ApplicationComponent
import com.abidria.presentation.common.injection.DaggerApplicationComponent

class AbidriaApplication : Application() {

    companion object {
        val injector: ApplicationComponent = DaggerApplicationComponent.builder().dataModule(DataModule()).build()
    }
}
