package com.abidria.data.common.injection

import com.abidria.BuildConfig
import com.abidria.data.experience.ExperienceRepository
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
    fun provideRetrofit() = Retrofit.Builder()
                                    .baseUrl(BuildConfig.API_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                    .build()

    @Provides
    @Singleton
    fun provideExperienceRepository(retrofit: Retrofit) = ExperienceRepository(retrofit)

}
