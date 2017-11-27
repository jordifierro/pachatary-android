package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ExperienceApi {

    @GET("/experiences/")
    fun experiences() : Flowable<Result<List<ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/experiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String) : Flowable<Result<ExperienceMapper>>
}
