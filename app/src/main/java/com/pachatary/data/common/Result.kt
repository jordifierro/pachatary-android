package com.pachatary.data.common

enum class Status { SUCCESS, IN_PROGRESS, ERROR }

fun <T> ResultSuccess(data: T? = null,
                      nextUrl: String? = null,
                      action: Request.Action = Request.Action.NONE,
                      params: Request.Params? = null) =
        Result(Status.SUCCESS, data = data, nextUrl = nextUrl,
               action = action, params = params, error = null)

fun <T> ResultError(error: Throwable,
                    data: T? = null,
                    nextUrl: String? = null,
                    action: Request.Action = Request.Action.NONE,
                    params: Request.Params? = null) =
        Result(Status.ERROR, data = data, nextUrl = nextUrl,
                action = action, params = params, error = error)

fun <T> ResultInProgress(data: T? = null,
                         nextUrl: String? = null,
                         action: Request.Action = Request.Action.NONE,
                         params: Request.Params? = null) =
        Result(Status.IN_PROGRESS, data = data, nextUrl = nextUrl,
                action = action, params = params, error = null)

data class Result<T>(val status: Status,
                     val data: T? = null,
                     val nextUrl: String? = null,
                     val action: Request.Action = Request.Action.NONE,
                     val params: Request.Params? = null,
                     val error: Throwable? = null) {

    fun hasBeenInitialized() = action != Request.Action.NONE
    fun isInProgress() = status == Status.IN_PROGRESS
    fun isSuccess() = status == Status.SUCCESS
    fun isError() = status == Status.ERROR
    fun hasMoreElements() = nextUrl != null

    fun builder() = Builder(this.status, this.data, this.nextUrl, this.action,
                            this.params, this.error)

    class Builder<T>(var status: Status, var data: T?, val nextUrl: String?,
                     var action: Request.Action, var params: Request.Params?,
                     var error: Throwable?) {

        fun status(status: Status): Builder<T> {
            this.status = status
            return this
        }

        fun data(data: T?): Builder<T> {
            this.data = data
            return this
        }

        fun action(action: Request.Action): Builder<T> {
            this.action = action
            return this
        }

        fun error(error: Throwable?): Builder<T> {
            this.error = error
            return this
        }

        fun params(params: Request.Params?): Builder<T> {
            this.params = params
            return this
        }

        fun build() = Result(this.status, this.data, this.nextUrl,
                             this.action, this.params, this.error)
    }
}
