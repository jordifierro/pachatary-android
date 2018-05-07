package com.pachatary.data.common

import com.pachatary.data.auth.ClientException
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher
import java.net.UnknownHostException

class NetworkParserFactory {

    companion object {
        fun <T> getTransformer() = ResultTransformer<T>()
        fun <T> getErrorTransformer(errorMapper: ((String) -> ClientException)) =
                ResultTransformer<T>(errorMapper)
        fun <T> getListTransformer() = ListResultTransformer<T>()
        fun <T, U: ToDomainMapper<T>> getPaginatedListTransformer() =
                PaginatedListResultTransformer<T, U>()
        fun getVoidTransformer() = VoidTransformer()
    }

    class ResultTransformer<T>(private val errorMapper: ((String) -> ClientException)? = null)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>, Result<T>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>>)
                : Publisher<Result<T>> =
                upstream.compose(CommonTransformer(parse, errorMapper, false))

        private val parse =
                { rxJavaResult: retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>> ->
                        Result(data = rxJavaResult.response()!!.body()!!.toDomain()) }
    }

    class ListResultTransformer<T>(private val errorMapper: ((String) -> ClientException)? = null)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out List<ToDomainMapper<T>>>,
                              Result<List<T>>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out List<ToDomainMapper<T>>>>)
                : Publisher<Result<List<T>>> =
                upstream.compose(CommonTransformer(parse, errorMapper, false))

        private val parse =
            { rxJavaResult: retrofit2.adapter.rxjava2.Result<out List<ToDomainMapper<T>>> ->
                Result(data = rxJavaResult.response()!!.body()!!.map { it.toDomain()!! },
                       error = null) }
    }

    class PaginatedListResultTransformer<T, U: ToDomainMapper<T>>(
            private val errorMapper: ((String) -> ClientException)? = null)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out PaginatedListMapper<T, U>>,
                              Result<List<T>>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out PaginatedListMapper<T, U>>>)
                : Publisher<Result<List<T>>> =
                upstream.compose(CommonTransformer(parse, errorMapper, false))

        private val parse =
            { rxJavaResult: retrofit2.adapter.rxjava2.Result<out PaginatedListMapper<T, U>> ->
                Result(data = rxJavaResult.response()!!.body()!!.results.map { it.toDomain() },
                       nextUrl = rxJavaResult.response()!!.body()!!.nextUrl, error = null) }
    }

    class VoidTransformer : FlowableTransformer<retrofit2.adapter.rxjava2.Result<Void>,
                                                Result<Void>> {

        override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<Void>>)
                : Publisher<Result<Void>> =
                upstream.compose(CommonTransformer(null, null, true))
    }

    class CommonTransformer<T, U>(
            private val parser: ((retrofit2.adapter.rxjava2.Result<out T>) -> Result<U>)?,
            private val errorMapper: ((String) -> ClientException)?,
            private val emptyBody: Boolean)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out T>, Result<U>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out T>>): Publisher<Result<U>> =
            upstream.map {
                if (it.isError) {
                    if (it.error() is UnknownHostException) Result<U>(null, error = it.error())
                    else throw it.error()!!
                }
                else if (it.response()!!.isSuccessful.not()) {
                    if (errorMapper == null) throw Exception(it.response()!!.errorBody()!!.string())
                    else Result<U>(data = null,
                            error = errorMapper.invoke(it.response()!!.errorBody()!!.string()))
                } else if (!emptyBody && parser != null) { parser.invoke(it) }
                else Result<U>(null)
            }
            .retry(2)
    }
}
