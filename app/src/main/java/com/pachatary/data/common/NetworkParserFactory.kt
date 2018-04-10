package com.pachatary.data.common

import com.pachatary.data.auth.ClientException
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class NetworkParserFactory {

    companion object {
        fun <T> getTransformer() = ResultTransformer<T>()
        fun <T> getErrorTransformer(errorMapper: ((String) -> ClientException)) = ResultTransformer<T>(errorMapper)
        fun <T> getListTransformer() = ListResultTransformer<T>()
        fun getVoidTransformer() = VoidTransformer()
    }

    class ResultTransformer<T>(val errorMapper: ((String) -> ClientException)? = null,
                               val emptyBody: Boolean = false)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>, Result<T>> {

        override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>>)
                : Publisher<Result<T>> =
                upstream.map {
                            if (it.isError()) throw it.error()!!
                            else if (it.response()!!.isSuccessful.not()) {
                                if (errorMapper == null) throw Exception(it.response()!!.errorBody()!!.string())
                                else Result<T>(data = null,
                                               error = errorMapper.invoke(it.response()!!.errorBody()!!.string()))
                            } else if (!emptyBody) Result(data = it.response()!!.body()!!.toDomain(), error = null)
                            else Result<T>(null)
                        }
                        .retry(2)
    }

    class ListResultTransformer<T>(val errorMapper: ((String) -> ClientException)? = null,
                                   val emptyBody: Boolean = false)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out List<ToDomainMapper<T>>>, Result<List<T>>> {

        override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<out List<ToDomainMapper<T>>>>)
                : Publisher<Result<List<T>>> =
                upstream.map {
                            if (it.isError()) throw it.error()!!
                            else if (it.response()!!.isSuccessful.not()) {
                                if (errorMapper == null) throw Exception(it.response()!!.errorBody()!!.string())
                                else Result<List<T>>(data = null,
                                                     error = errorMapper.invoke(it.response()!!.errorBody()!!.string()))
                            } else if (!emptyBody)
                                Result(data = it.response()!!.body()!!.map { it.toDomain()!! }, error = null)
                            else Result<List<T>>(null)
                        }
                        .retry(2)
    }

    class VoidTransformer : FlowableTransformer<retrofit2.adapter.rxjava2.Result<Void>, Result<Void>> {

        override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<Void>>): Publisher<Result<Void>> =
                upstream.map {
                            if (it.isError()) throw it.error()!!
                            else if (it.response()!!.isSuccessful.not())
                                throw Exception(it.response()!!.errorBody()!!.string())
                            else Result<Void>(null)
                        }
                        .retry(2)
    }
}
