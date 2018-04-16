package com.pachatary.data.experience

import com.pachatary.data.common.PaginatedListMapper
import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ExperienceApi {

    @GET("/experiences/?mine=false")
    fun exploreExperiences(): Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET("/experiences/?mine=true")
    fun myExperiences(): Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET("/experiences/?saved=true")
    fun savedExperiences(): Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @GET
    fun paginateExperiences(@Url url: String)
            : Flowable<Result<PaginatedListMapper<Experience, ExperienceMapper>>>

    @FormUrlEncoded
    @POST("/experiences/")
    fun createExperience(@Field("title") title: String,
                         @Field("description") description: String): Flowable<Result<ExperienceMapper>>

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
