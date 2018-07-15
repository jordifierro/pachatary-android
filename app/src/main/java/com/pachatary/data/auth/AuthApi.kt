package com.pachatary.data.auth

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface AuthApi {

    @FormUrlEncoded
    @POST("/people/")
    fun createPersonResource(@Field("client_secret_key") clientSecretKey: String)
                                                                : Flowable<Result<AuthTokenMapper>>

    @FormUrlEncoded
    @PATCH("/people/me")
    fun register(@Field("username") username: String,
                 @Field("email") email: String): Flowable<Result<Void>>

    @FormUrlEncoded
    @POST("/people/me/email-confirmation")
    fun confirmEmail(@Field("confirmation_token") confirmationToken: String): Flowable<Result<Void>>

    @FormUrlEncoded
    @POST("/people/me/login-email")
    fun askLoginEmail(@Field("email") email: String): Flowable<Result<Void>>

    @FormUrlEncoded
    @POST("/people/me/login")
    fun login(@Field("token") loginToken: String): Flowable<Result<AuthTokenMapper>>

    @GET("/client-versions")
    fun clientVersions(): Flowable<Result<ClientVersionsMapper>>
}
