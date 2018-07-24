package com.pachatary.data.common

import com.pachatary.data.auth.NoLoggedException
import okhttp3.Interceptor
import okhttp3.Response


class ClientVersionHttpInterceptor(val versionCode: Int) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "And-" + String.format("%03d", versionCode))
                    .build()
            return chain.proceed(request)
        } catch (e: NoLoggedException) {
            return chain.proceed(chain.request())
        }
    }
}
