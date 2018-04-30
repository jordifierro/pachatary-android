package com.pachatary.data.common

data class Result<T>(val data: T?,
                     val nextUrl: String? = null,
                     val action: Request.Action = Request.Action.NONE,
                     val params: Request.Params? = null,
                     val inProgress: Boolean = false,
                     val error: Throwable? = null) {

    fun hasBeenInitialized() = action != Request.Action.NONE
    fun isInProgress() = inProgress
    fun isSuccess() = !isInProgress() && error == null
    fun isError() = !isInProgress() && error != null
    fun hasMoreElements() = nextUrl != null

    fun builder() = Builder(this.data, this.nextUrl, this.action,
                            this.params, this.inProgress, this.error)

    class Builder<T>(var data: T?, val nextUrl: String?, var action: Request.Action,
                     var params: Request.Params?, var inProgress: Boolean, var error: Throwable?) {

        fun data(data: T?): Builder<T> {
            this.data = data
            return this
        }

        fun action(action: Request.Action): Builder<T> {
            this.action = action
            return this
        }

        fun inProgress(progress: Boolean): Builder<T> {
            this.inProgress = progress
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

        fun build() = Result(this.data, this.nextUrl, this.action,
                             this.params, this.inProgress, this.error)
    }
}
