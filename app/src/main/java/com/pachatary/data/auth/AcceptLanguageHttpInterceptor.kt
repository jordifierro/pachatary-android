package com.pachatary.data.auth

import okhttp3.Interceptor
import okhttp3.Response


class AcceptLanguageHttpInterceptor(val language: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request().newBuilder()
                    .addHeader("Accept-Language", language.replace("_", "-"))
                    .build()
            return chain.proceed(request)
        } catch (e: NoLoggedException) {
            return chain.proceed(chain.request())
        }
    }
}
