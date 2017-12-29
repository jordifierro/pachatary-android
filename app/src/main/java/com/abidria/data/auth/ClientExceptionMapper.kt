package com.abidria.data.auth

import com.google.gson.JsonParser

data class ClientExceptionMapper(val errorBodyString: String) {

    fun toError(): ClientException {
        val jsonElement = JsonParser().parse(errorBodyString)
        val jsonError = jsonElement.asJsonObject.get("error").asJsonObject
        val source = jsonError.get("source").asString
        val code = jsonError.get("code").asString
        val message = jsonError.get("message").asString
        return ClientException(source, code, message)
    }
}
