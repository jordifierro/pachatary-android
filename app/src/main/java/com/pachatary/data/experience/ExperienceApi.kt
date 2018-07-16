package com.pachatary.data.experience

import com.pachatary.data.common.PaginatedListMapper
import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ExperienceApi {

    @GET("/experiences/search")
    fun exploreExperiences(@Query("word") word: String?,
                           @Query("latitude") latitude: Double?,
                           @Query("longitude") longitude: Double?)
                               : Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET("/experiences/?username=self")
    fun myExperiences(): Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET("/experiences/?saved=true")
    fun savedExperiences(): Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET("/experiences/")
    fun personsExperiences(@Query("username") username: String)
                               : Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET
    fun paginateExperiences(@Url url: String)
                               : Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/experiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String)
                                                                : Flowable<Result<ExperienceMapper>>

    @GET("/experiences/{experience_id}")
    fun getExperience(@Path("experience_id") experienceId: String)
                                                                : Flowable<Result<ExperienceMapper>>

    @GET("/experiences/{experience_share_id}/id")
    fun translateShareId(@Path("experience_share_id") experienceShareId: String)
                                                              : Flowable<Result<ExperienceIdMapper>>

    @FormUrlEncoded
    @PATCH("/experiences/{id}")
    fun editExperience(@Path("id") id: String,
                       @Field("title") title: String,
                       @Field("description") description: String)
                                                                : Flowable<Result<ExperienceMapper>>

    @POST("/experiences/{id}/save/")
    fun saveExperience(@Path("id") experienceId: String): Flowable<Result<Void>>

    @DELETE("/experiences/{id}/save/")
    fun unsaveExperience(@Path("id") experienceId: String): Flowable<Result<Void>>
}
