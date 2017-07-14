package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.GET

interface ExperienceApi {

    @GET("/experiences/")
    fun experiences() : Flowable<Result<List<ExperienceMapper>>>
}
