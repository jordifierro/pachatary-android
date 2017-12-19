package com.abidria.data.auth

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


class AuthHttpInterceptor(val authStorageRepository: AuthStorageRepository) : Interceptor {

    data class AuthHeader(val key: String, val value: String)

    fun getAuthHeader() = AuthHeader(key = "Authorization",
                                     value = "Token " + authStorageRepository.getPersonCredentials().accessToken)

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val authHeader = getAuthHeader()
            val request = chain.request().newBuilder()
                    .addHeader(authHeader.key, authHeader.value)
                    .build()
            return chain.proceed(request)
        } catch (e: NoLoggedException) {
            return chain.proceed(chain.request())
        }
    }
}
