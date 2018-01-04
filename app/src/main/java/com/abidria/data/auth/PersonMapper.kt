package com.abidria.data.auth

import com.abidria.data.common.ToDomainMapper

data class PersonMapper(val isRegistered: Boolean, val username: String,
                        val email: String, val isEmailConfirmed: Boolean) : ToDomainMapper<Person> {

    override fun toDomain() = Person(isRegistered, username, email, isEmailConfirmed)
}
