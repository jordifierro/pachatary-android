package com.pachatary.data.profile

import io.reactivex.Flowable
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface ProfileApi {

    @GET("/profiles/self")
    fun selfProfile() : Flowable<Result<ProfileMapper>>

    @GET("/profiles/{username}")
    fun profile(@Path("username") username: String): Flowable<Result<ProfileMapper>>

    @FormUrlEncoded
    @PATCH("/profiles/self")
    fun editProfile(@Field("bio") bio: String): Flowable<Result<ProfileMapper>>
}
