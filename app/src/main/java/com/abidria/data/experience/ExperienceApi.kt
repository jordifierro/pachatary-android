package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ExperienceApi {

    @GET("/exploreExperiences/?mine=false")
    fun exploreExperiences() : Flowable<Result<List<ExperienceMapper>>>

    @GET("/exploreExperiences/?mine=true")
    fun myExperiences() : Flowable<Result<List<ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/exploreExperiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String) : Flowable<Result<ExperienceMapper>>

    @FormUrlEncoded
    @PATCH("/exploreExperiences/{id}")
    fun editExperience(@Path("id") id: String,
                       @Field("title") title: String,
                       @Field("description") description: String): Flowable<Result<ExperienceMapper>>
}
