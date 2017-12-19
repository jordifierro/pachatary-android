package com.abidria.data.auth

data class AuthTokenMapper(val accessToken: String, val refreshToken: String) {

    fun toDomain() = AuthToken(accessToken = this.accessToken, refreshToken = this.refreshToken)
}
