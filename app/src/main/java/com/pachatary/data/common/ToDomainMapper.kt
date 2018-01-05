package com.pachatary.data.common

interface ToDomainMapper<U> {
    fun toDomain(): U?
}
