package com.pachatary.data.common

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkParserFactory {

    companion object {
        fun <T> getTransformer() = ResultTransformer<T>()
        fun <T> getErrorTransformer(errorMapper: ((String) -> ClientException)) =
                ResultTransformer<T>(errorMapper)
        fun <T> getListTransformer() = ListResultTransformer<T>()
        fun <T, U: ToDomainMapper<T>> getPaginatedListTransformer() =
                PaginatedListResultTransformer<T, U>()
        fun getVoidTransformer(errorMapper: ((String) -> ClientException)? = null)
                = VoidTransformer(errorMapper)
    }

    class ResultTransformer<T>(private val errorMapper: ((String) -> ClientException)? = null)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>, Result<T>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>>>)
                : Publisher<Result<T>> =
                upstream.compose(CommonTransformer(parse, errorMapper, false))

        private val parse =
                { rxJavaResult: retrofit2.adapter.rxjava2.Result<out ToDomainMapper<T>> ->
                        ResultSuccess(rxJavaResult.response()!!.body()!!.toDomain()) }
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
                ResultSuccess(rxJavaResult.response()!!.body()!!.map { it.toDomain()!! }) }
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
                ResultSuccess(rxJavaResult.response()!!.body()!!.results.map { it.toDomain() },
                              nextUrl = rxJavaResult.response()!!.body()!!.nextUrl) }
    }

    class VoidTransformer(private val errorMapper: ((String) -> ClientException)? = null)
                       : FlowableTransformer<retrofit2.adapter.rxjava2.Result<Void>, Result<Void>> {

        override fun apply(upstream: Flowable<retrofit2.adapter.rxjava2.Result<Void>>)
                : Publisher<Result<Void>> =
                upstream.compose(CommonTransformer(null, errorMapper, true))
    }

    class CommonTransformer<T, U>(
            private val parser: ((retrofit2.adapter.rxjava2.Result<out T>) -> Result<U>)?,
            private val errorMapper: ((String) -> ClientException)?,
            private val emptyBody: Boolean)
        : FlowableTransformer<retrofit2.adapter.rxjava2.Result<out T>, Result<U>> {

        override fun apply(
                upstream: Flowable<retrofit2.adapter.rxjava2.Result<out T>>): Flowable<Result<U>> =
            upstream.map {
                if (it.isError) throw it.error()!!
                else if (it.response()!!.isSuccessful.not()) {
                    if (errorMapper == null) throw Exception(it.response()!!.errorBody()!!.string())
                    else ResultError(errorMapper.invoke(it.response()!!.errorBody()!!.string()))
                } else if (!emptyBody && parser != null) { parser.invoke(it) }
                else ResultSuccess()
            }
            .retry(2)
            .onErrorResumeNext { error: Throwable ->
                if (error is UnknownHostException || error is SocketTimeoutException)
                    Flowable.just(ResultError(error))
                else Flowable.error(error)
            }
    }
}
