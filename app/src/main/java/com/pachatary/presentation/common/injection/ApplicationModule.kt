package com.pachatary.presentation.common.injection

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.support.v4.content.res.ResourcesCompat
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import com.pachatary.presentation.common.view.PictureDeviceCompat
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named
import javax.inject.Singleton


@Module
class ApplicationModule(val application: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application = application

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Named("io")
    fun providerIOScheduler(): Scheduler = Schedulers.io()

    @Provides
    @Named("myexperiences")
    fun providerMainScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @Provides
    fun provideSchedulerProvider(@Named("io") subscriberScheduler: Scheduler,
                                 @Named("myexperiences") observerScheduler: Scheduler): SchedulerProvider =
        SchedulerProvider(subscriberScheduler, observerScheduler)

    @Provides
    @Singleton
    @Named("device_width")
    fun provideDeviceWidth() = Resources.getSystem().displayMetrics.widthPixels

    @Provides
    @Singleton
    fun providePictureDeviceCompat(@Named("device_width") deviceWidth: Int): PictureDeviceCompat =
            PictureDeviceCompat(deviceWidth)
}
