package com.abidria.data.common

import com.abidria.data.auth.ClientException
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher


fun <T, U> ParseSuccessResultTransformer(mapper: (T) -> U) = ParseNetworkResultTransformer(mapper, null)
fun ParseSuccessEmptyBodyResultTransformer() =
        ParseNetworkResultTransformer<Void, Void>(null, null, true)

class ParseNetworkResultTransformer<T, U>(val mapper: ((T) -> U)?, val errorMapper: ((String) -> ClientException)?,
                                          val emptyBody: Boolean = false)
                                            : FlowableTransformer<retrofit2.adapter.rxjava2.Result<T>, Result<U>> {




    override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<T>>): Publisher<Result<U>> =
        upstream
                .map {
                    if (it.isError()) throw it.error()!!
                    else if (it.response()!!.isSuccessful.not()) {
                        if (errorMapper == null) throw Exception(it.response()!!.errorBody()!!.string())
                        else Result<U>(data = null, error = errorMapper.invoke(it.response()!!.errorBody()!!.string()))
                    } else if (!emptyBody) Result(data = mapper!!.invoke(it.response()!!.body()!!), error = null)
                    else Result<U>(null, null)
                }
                .retry(2)
}

