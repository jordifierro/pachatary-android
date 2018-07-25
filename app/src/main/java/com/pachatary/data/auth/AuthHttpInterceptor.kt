package com.pachatary.data.auth

import com.pachatary.data.common.Header
import okhttp3.Interceptor
import okhttp3.Response


class AuthHttpInterceptor(val authStorageRepository: AuthStorageRepository) : Interceptor, Header {

    override fun key() = "Authorization"
    override fun value() = "Token " + authStorageRepository.getPersonCredentials().accessToken

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request().newBuilder()
                    .addHeader(key(), value())
                    .build()
            return chain.proceed(request)
        } catch (e: NoLoggedException) {
            return chain.proceed(chain.request())
        }
    }
}
