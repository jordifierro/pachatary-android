package com.pachatary.data.auth

import com.pachatary.data.common.ClientExceptionMapper
import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import io.reactivex.Flowable
import io.reactivex.Scheduler
import retrofit2.Retrofit
import javax.inject.Named

class AuthApiRepository (retrofit: Retrofit, private val clientSecretKey: String,
                         @Named("io") private val ioScheduler: Scheduler) {

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    fun getPersonInvitation(): Flowable<Result<AuthToken>> =
        authApi.createPersonResource(clientSecretKey = clientSecretKey)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getTransformer())
                .startWith(ResultInProgress())

    fun register(username: String, email: String): Flowable<Result<Void>> =
        authApi.register(username = username, email = email)
                .compose(NetworkParserFactory
                                .getVoidTransformer { ClientExceptionMapper(it).toError() })

    fun confirmEmail(confirmationToken: String): Flowable<Result<Void>> =
        authApi.confirmEmail(confirmationToken = confirmationToken)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory
                                .getVoidTransformer { ClientExceptionMapper(it).toError() })
                .startWith(ResultInProgress())

    fun askLoginEmail(email: String): Flowable<Result<Void>> =
        authApi.askLoginEmail(email)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getVoidTransformer())
                .startWith(ResultInProgress())

    fun login(loginToken: String): Flowable<Result<AuthToken>> =
        authApi.login(loginToken)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getErrorTransformer(
                        { ClientExceptionMapper(it).toError() }))
                .startWith(ResultInProgress())

    fun clientVersions(): Flowable<Result<Int>> =
            authApi.clientVersions()
                    .subscribeOn(ioScheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())
}
