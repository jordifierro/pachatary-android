package com.abidria.data.auth

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @FormUrlEncoded
    @POST("/people/")
    fun createPersonResource(@Field("client_secret_key") clientSecretKey: String)
            : Flowable<Result<AuthTokenMapper>>
}
