package com.abidria.data.common

interface ToDomainMapper<U> {
    fun toDomain(): U?
}
