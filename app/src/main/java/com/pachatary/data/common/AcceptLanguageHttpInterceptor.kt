package com.pachatary.data.common

import com.pachatary.data.auth.NoLoggedException
import okhttp3.Interceptor
import okhttp3.Response


class AcceptLanguageHttpInterceptor(val language: String) : Interceptor, Header {

    override fun key() = "Accept-Language"
    override fun value() = language.replace("_", "-")

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
