package com.abidria.presentation.common

import android.app.Application
import com.abidria.BuildConfig
import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.common.injection.ApplicationComponent
import com.abidria.presentation.common.injection.ApplicationModule
import com.abidria.presentation.common.injection.DaggerApplicationComponent
import com.facebook.stetho.Stetho
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.okhttp.OkHttpStack



class AbidriaApplication : Application() {

    companion object {
        lateinit var injector: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()

        injector = DaggerApplicationComponent.builder()
                                                .applicationModule(ApplicationModule(this))
                                                .dataModule(DataModule())
                                             .build()

        Stetho.initializeWithDefaults(this);

        val picassoBuilder = Picasso.Builder(this)
        picassoBuilder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        picassoBuilder.indicatorsEnabled(true)
        Picasso.setSingletonInstance(picassoBuilder.build())

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.HTTP_STACK = OkHttpStack()
    }
}
