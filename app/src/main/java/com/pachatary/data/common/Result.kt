package com.pachatary.data.common

data class Result<T>(val data: T?, val error: Throwable?) {

    fun isError(): Boolean = error != null
    fun isSuccess(): Boolean = !isError()
}
