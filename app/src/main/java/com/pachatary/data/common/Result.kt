package com.pachatary.data.common

data class Result<T>(val data: T?,
                     val nextUrl: String? = null,
                     val lastEvent: Event = Event.NONE,
                     val inProgress: Boolean = false,
                     val error: Throwable? = null) {

    enum class Event {
        NONE, GET_FIRSTS, PAGINATE, REFRESH
    }

    fun hasBeenInitialized() = lastEvent != Event.NONE
    fun isInProgress() = inProgress
    fun isSuccess() = !isInProgress() && error == null
    fun isError() = !isInProgress() && error != null
    fun hasMoreElements() = nextUrl != null

    fun builder() = Builder(this.data, this.nextUrl, this.lastEvent, this.inProgress, this.error)

    class Builder<T>(var data: T?, val nextUrl: String?, var lastEvent: Event,
                     var inProgress: Boolean, var error: Throwable?) {

        fun data(data: T?): Builder<T> {
            this.data = data
            return this
        }

        fun lastEvent(event: Event): Builder<T> {
            this.lastEvent = event
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

        fun build() = Result(this.data, this.nextUrl, this.lastEvent, this.inProgress, this.error)
    }
}
