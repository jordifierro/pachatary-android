package com.abidria.data.auth

import com.abidria.data.common.ParseNetworkResultTransformer
import com.abidria.data.common.Result
import io.reactivex.Flowable
import retrofit2.Retrofit

class AuthApiRepository (retrofit: Retrofit, val clientSecretKey: String) {

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    fun getPersonInvitation(): Flowable<Result<AuthToken>> {
        return authApi.createPersonResource(clientSecretKey = clientSecretKey)
                .compose(ParseNetworkResultTransformer({ it.toDomain() }))
    }

}
