package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ExperienceApi {

    @GET("/experiences/?mine=false")
    fun exploreExperiences() : Flowable<Result<List<ExperienceMapper>>>

    @GET("/experiences/?mine=true")
    fun myExperiences() : Flowable<Result<List<ExperienceMapper>>>

    @GET("/experiences/?saved=true")
    fun savedExperiences() : Flowable<Result<List<ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/experiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String) : Flowable<Result<ExperienceMapper>>

    @FormUrlEncoded
    @PATCH("/experiences/{id}")
    fun editExperience(@Path("id") id: String,
                       @Field("title") title: String,
                       @Field("description") description: String): Flowable<Result<ExperienceMapper>>

    @POST("/experiences/{id}/save/")
    fun saveExperience(@Path("id") experienceId: String): Flowable<Result<Void>>

    @DELETE("/experiences/{id}/save/")
    fun unsaveExperience(@Path("id") experienceId: String): Flowable<Result<Void>>
}
