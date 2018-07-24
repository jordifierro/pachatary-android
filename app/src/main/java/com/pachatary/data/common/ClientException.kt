package com.pachatary.data.common

data class ClientException(val source: String, val code: String,
                           override val message: String): Exception()
