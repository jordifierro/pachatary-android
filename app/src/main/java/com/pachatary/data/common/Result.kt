package com.pachatary.data.common

enum class Event {
    NONE, GET_FIRSTS, PAGINATE, REFRESH
}

data class Result<T>(val data: T?,
                     val lastEvent: Event = Event.NONE,
                     val inProgress: Boolean = false,
                     val error: Throwable? = null) {

    fun hasNotBeenInitialized() = lastEvent == Event.NONE
    fun isInProgress() = inProgress
    fun isSuccess() = !isInProgress() && error == null
    fun isError() = !isInProgress() && error != null

    fun builder() = Builder(this.data, this.lastEvent, this.inProgress, this.error)

    class Builder<T>(var data: T?, var lastEvent: Event,
                     val inProgress: Boolean, val error: Throwable?) {

        fun data(data: T?): Builder<T> {
            this.data = data
            return this
        }

        fun lastEvent(event: Event): Builder<T> {
            this.lastEvent = event
            return this
        }

        fun build() = Result(this.data, this.lastEvent, this.inProgress, this.error)
    }
}
