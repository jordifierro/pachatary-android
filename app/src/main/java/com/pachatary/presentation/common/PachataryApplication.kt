package com.pachatary.presentation.common

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.google.android.gms.security.ProviderInstaller
import com.jakewharton.picasso.OkHttp3Downloader
import com.pachatary.BuildConfig
import com.pachatary.data.common.injection.DataModule
import com.pachatary.presentation.common.injection.ApplicationComponent
import com.pachatary.presentation.common.injection.ApplicationModule
import com.pachatary.presentation.common.injection.DaggerApplicationComponent
import com.squareup.picasso.Picasso
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.okhttp.OkHttpStack

class PachataryApplication : MultiDexApplication() {

    companion object {
        lateinit var injector: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        ProviderInstaller.installIfNeeded(this)

        injector = DaggerApplicationComponent.builder()
                                                .applicationModule(ApplicationModule(this))
                                                .dataModule(DataModule())
                                             .build()

        val picassoBuilder = Picasso.Builder(this)
        picassoBuilder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        if (BuildConfig.DEBUG) picassoBuilder.indicatorsEnabled(true)
        Picasso.setSingletonInstance(picassoBuilder.build())

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID
        UploadService.HTTP_STACK = OkHttpStack()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        if (BuildConfig.DEBUG) MultiDex.install(this)
    }
}
