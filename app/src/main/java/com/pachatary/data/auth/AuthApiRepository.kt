package com.pachatary.data.auth

import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.Scheduler
import retrofit2.Retrofit
import javax.inject.Named

class AuthApiRepository (retrofit: Retrofit, val clientSecretKey: String,
                         @Named("io") val ioScheduler: Scheduler) {

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    fun getPersonInvitation(): Flowable<Result<AuthToken>> {
        return authApi.createPersonResource(clientSecretKey = clientSecretKey)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getTransformer())
                .startWith(Result<AuthToken>(null, inProgress = true))
    }

    fun register(username: String, email: String): Flowable<Result<Person>> {
        return authApi.register(username = username, email = email)
                .compose(NetworkParserFactory.getErrorTransformer({ ClientExceptionMapper(it).toError() }))
    }

    fun confirmEmail(confirmationToken: String): Flowable<Result<Person>> {
        return authApi.confirmEmail(confirmationToken = confirmationToken)
                .compose(NetworkParserFactory.getErrorTransformer({ ClientExceptionMapper(it).toError() }))
    }
}
