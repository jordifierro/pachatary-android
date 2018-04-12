package com.pachatary.data.common.injection

import android.content.Context
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthApiRepository
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.AuthStorageRepository
import com.pachatary.data.common.ResultStreamFactory
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceApiRepository
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneApiRepository
import com.pachatary.data.scene.SceneRepository
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.experience.NewExperienceRepository
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
    fun provideAuthHttpInterceptor(authStorageRepository: AuthStorageRepository) =
            AuthHttpInterceptor(authStorageRepository)

    @Provides
    @Singleton
    fun provideOkHttpClient(authHttpInterceptor: AuthHttpInterceptor): OkHttpClient = OkHttpClient.Builder()
                                                          .addNetworkInterceptor(StethoInterceptor())
                                                          .addInterceptor(authHttpInterceptor)
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
    fun provideExperienceApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler, context: Context,
                                       authHttpInterceptor: AuthHttpInterceptor): ExperienceApiRepository =
            ExperienceApiRepository(retrofit, scheduler, context, authHttpInterceptor)

    @Provides
    @Singleton
    fun provideExperienceStreamFactory() = ResultStreamFactory<Experience>()

    @Provides
    @Singleton
    fun provideNewExperienceStreamFactory() = NewResultStreamFactory<Experience>()

    @Provides
    @Singleton
    fun provideExperienceRepository(apiRepository: ExperienceApiRepository, @Named("io") scheduler: Scheduler,
                                    experienceStreamFactory: ResultStreamFactory<Experience>): ExperienceRepository =
            ExperienceRepository(apiRepository, scheduler, experienceStreamFactory)

    @Provides
    @Singleton
    fun provideNewExperienceRepository(apiRepository: ExperienceApiRepository, @Named("io") scheduler: Scheduler,
                                       experienceStreamFactory: NewResultStreamFactory<Experience>): NewExperienceRepository =
            NewExperienceRepository(apiRepository, experienceStreamFactory)

    @Provides
    @Singleton
    fun provideSceneApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler,
                                  context: Context, authHttpInterceptor: AuthHttpInterceptor): SceneApiRepository =
            SceneApiRepository(retrofit, scheduler, context, authHttpInterceptor)

    @Provides
    @Singleton
    fun provideScenesStreamFactory() = ResultStreamFactory<Scene>()

    @Provides
    @Singleton
    fun provideNewScenesStreamFactory() = NewResultStreamFactory<Scene>()

    @Provides
    @Singleton
    fun provideSceneRepository(apiRepository: SceneApiRepository,
                               sceneStreamFactory: NewResultStreamFactory<Scene>): SceneRepository =
            SceneRepository(apiRepository, sceneStreamFactory)

    @Provides
    @Singleton
    fun provideAuthStorageRepository(context: Context) = AuthStorageRepository(context)

    @Provides
    @Singleton
    fun provideAuthApiRepository(retrofit: Retrofit) = AuthApiRepository(retrofit, BuildConfig.CLIENT_SECRET_KEY)

    @Provides
    @Singleton
    fun provideAuthRepository(authStorageRepository: AuthStorageRepository,
                              authApiRepository: AuthApiRepository) =
            AuthRepository(authStorageRepository, authApiRepository)
}
