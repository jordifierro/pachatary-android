package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ExperienceApi {

    @GET("/experiences/")
    fun experiences() : Flowable<Result<List<ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/experiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String) : Flowable<Result<ExperienceMapper>>

    @FormUrlEncoded
    @PATCH("/experiences/{id}")
    fun editExperience(@Path("id") id: String,
                       @Field("title") title: String,
                       @Field("description") description: String): Flowable<Result<ExperienceMapper>>
}
