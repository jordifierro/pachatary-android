package com.pachatary.data.auth

import com.pachatary.data.common.ToDomainMapper

data class AuthTokenMapper(val accessToken: String, val refreshToken: String) : ToDomainMapper<AuthToken> {

    override fun toDomain() = AuthToken(accessToken = this.accessToken, refreshToken = this.refreshToken)
}
