package com.pachatary.data.auth

data class ClientException(val source: String, val code: String,
                           override val message: String): Exception()
