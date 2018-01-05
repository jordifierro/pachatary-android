package com.pachatary.data.auth

import com.pachatary.data.common.ToDomainMapper

data class PersonMapper(val isRegistered: Boolean, val username: String,
                        val email: String, val isEmailConfirmed: Boolean) : ToDomainMapper<Person> {

    override fun toDomain() = Person(isRegistered, username, email, isEmailConfirmed)
}
