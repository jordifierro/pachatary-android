package com.pachatary.data.auth

import com.pachatary.data.common.ToDomainMapper

data class ClientVersionsMapper(val android: AndroidClient) : ToDomainMapper<Int> {

    data class AndroidClient(val minVersion: Int): ToDomainMapper<Int> {
        override fun toDomain() = minVersion
    }

    override fun toDomain() = android.toDomain()
}
