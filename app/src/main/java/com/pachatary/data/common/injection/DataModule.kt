package com.pachatary.data.common.injection

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pachatary.BuildConfig
import com.pachatary.data.auth.*
import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.experience.*
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneApiRepository
import com.pachatary.data.scene.SceneRepository
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
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
    fun provideClientVersionHttpInterceptor() =
            ClientVersionHttpInterceptor(BuildConfig.VERSION_CODE)

    @Provides
    @Singleton
    fun provideAcceptLanguageHttpInterceptor() =
            AcceptLanguageHttpInterceptor(Locale.getDefault().toString())

    @Provides
    @Singleton
    fun provideOkHttpClient(authHttpInterceptor: AuthHttpInterceptor,
                            clientVersionHttpInterceptor: ClientVersionHttpInterceptor,
                            acceptLanguageHttpInterceptor: AcceptLanguageHttpInterceptor) =
            OkHttpClient.Builder()
                  .addNetworkInterceptor(StethoInterceptor())
                  .addInterceptor(authHttpInterceptor)
                  .addInterceptor(clientVersionHttpInterceptor)
                  .addInterceptor(acceptLanguageHttpInterceptor)
                  .build()

    @Provides
    @Singleton
    fun provideGsonConverter(): Converter.Factory = GsonConverterFactory.create(
            GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                         .create())

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gsonConverterFactory: Converter.Factory) =
            Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build()

    @Provides
    @Singleton
    fun provideExperienceApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler,
                                       context: Context, authHttpInterceptor: AuthHttpInterceptor) =
            ExperienceApiRepository(retrofit, scheduler, context, authHttpInterceptor)

    @Provides
    @Singleton
    fun provideExperienceCacheFactory() = ResultCacheFactory<Experience>()

    @Provides
    @Singleton
    fun provideExperienceRequesterFactory(apiRepository: ExperienceApiRepository) =
            ExperienceRequesterFactory(apiRepository)

    @Provides
    @Singleton
    fun provideExperienceRepoSwitch(experienceCacheFactory: ResultCacheFactory<Experience>,
                                    requesterFactory: ExperienceRequesterFactory) =
            ExperienceRepoSwitch(experienceCacheFactory, requesterFactory)

    @Provides
    @Singleton
    fun provideExperienceRepository(apiRepository: ExperienceApiRepository,
                                    experienceRepoSwitch: ExperienceRepoSwitch) =
            ExperienceRepository(apiRepository, experienceRepoSwitch)

    @Provides
    @Singleton
    fun provideSceneApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler,
                                  context: Context, authHttpInterceptor: AuthHttpInterceptor) =
            SceneApiRepository(retrofit, scheduler, context, authHttpInterceptor)

    @Provides
    @Singleton
    fun provideScenesCacheFactory() = ResultCacheFactory<Scene>()

    @Provides
    @Singleton
    fun provideSceneRepository(apiRepository: SceneApiRepository,
                               sceneCacheFactory: ResultCacheFactory<Scene>): SceneRepository =
            SceneRepository(apiRepository, sceneCacheFactory)

    @Provides
    @Singleton
    fun provideAuthStorageRepository(context: Context) = AuthStorageRepository(context)

    @Provides
    @Singleton
    fun provideAuthApiRepository(retrofit: Retrofit, @Named("io") scheduler: Scheduler) =
            AuthApiRepository(retrofit, BuildConfig.CLIENT_SECRET_KEY, scheduler)

    @Provides
    @Singleton
    fun provideAuthRepository(authStorageRepository: AuthStorageRepository,
                              authApiRepository: AuthApiRepository) =
            AuthRepository(authStorageRepository, authApiRepository, BuildConfig.VERSION_CODE)
}
