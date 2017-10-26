package com.abidria.data.scene

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface SceneApi {

    @GET("/scenes/")
    fun scenes(@Query("experience") experienceId: String): Flowable<Result<List<SceneMapper>>>

    @FormUrlEncoded
    @POST("/scenes/")
    fun createScene(@Field("title") title: String,
                    @Field("description") description: String,
                    @Field("latitude") latitude: Double,
                    @Field("longitude") longitude: Double,
                    @Field("experience_id") experienceId: String): Flowable<Result<SceneMapper>>
}
