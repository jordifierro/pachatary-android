package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.http.GET

interface ExperienceApi {

    @GET("/experiences/")
    fun experiences() : Flowable<List<ExperienceMapper>>
}
