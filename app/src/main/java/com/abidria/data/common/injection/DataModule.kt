package com.abidria.data.common.injection

import com.abidria.BuildConfig
import com.abidria.data.experience.ExperienceApiRepository
import com.abidria.data.experience.ExperienceRepository
import com.abidria.data.scene.SceneApiRepository
import com.abidria.data.scene.SceneRepository
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
                                                          .addNetworkInterceptor(StethoInterceptor())
                                                          .build()

    @Provides
    @Singleton
    fun provideGsonConverter(): Converter.Factory = GsonConverterFactory.create(
            GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) .create())

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gsonConverterFactory: Converter.Factory): Retrofit =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build()

    @Provides
    @Singleton
    fun provideExperienceApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler): ExperienceApiRepository =
            ExperienceApiRepository(retrofit, scheduler)

    @Provides
    @Singleton
    fun provideExperienceRepository(apiRepository: ExperienceApiRepository): ExperienceRepository =
            ExperienceRepository(apiRepository)

    @Provides
    @Singleton
    fun provideSceneApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler): SceneApiRepository =
            SceneApiRepository(retrofit, scheduler)

    @Provides
    @Singleton
    fun provideSceneRepository(apiRepository: SceneApiRepository): SceneRepository =
            SceneRepository(apiRepository)
}
