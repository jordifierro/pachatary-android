package com.abidria.data.common

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher


class ParseNetworkResultTransformer<T, U>(val mapper: (T) -> U)
                                            : FlowableTransformer<retrofit2.adapter.rxjava2.Result<T>, Result<U>> {


    override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<T>>): Publisher<Result<U>> =
        upstream
                .map {
                    if (it.isError()) throw it.error()!!
                    else if (it.response()!!.isSuccessful.not()) throw Exception(it.response()!!.errorBody()!!.string())
                    else Result(data = mapper(it.response()!!.body()!!), error = null) }
                .retry(2)
}

