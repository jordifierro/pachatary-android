package com.pachatary.data.common

data class ClientVersionsMapper(val android: AndroidClient) : ToDomainMapper<Int> {

    data class AndroidClient(val minVersion: Int): ToDomainMapper<Int> {
        override fun toDomain() = minVersion
    }

    override fun toDomain() = android.toDomain()
}
