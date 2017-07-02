package com.abidria.data.common.injection

import com.abidria.BuildConfig
import com.abidria.data.experience.ExperienceRepository
import com.abidria.data.scene.SceneRepository
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun provideRetrofit() =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()

    @Provides
    @Singleton
    fun provideExperienceRepository(retrofit: Retrofit) = ExperienceRepository(retrofit)

    @Provides
    @Singleton
    fun provideSceneRepository(retrofit: Retrofit) = SceneRepository(retrofit)
}
