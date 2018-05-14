package com.pachatary.data.common

data class Request(val action: Action, val params: Params? = null) {

    enum class Action {
        NONE, GET_FIRSTS, PAGINATE, REFRESH
    }

    data class Params(val word: String? = null,
                      val latitude: Double? = null,
                      val longitude: Double? = null,
                      val username: String? = null)
}