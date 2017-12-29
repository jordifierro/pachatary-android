package com.abidria.data.auth

data class PersonMapper(val isRegistered: Boolean, val username: String,
                        val email: String, val isEmailConfirmed: Boolean) {

    fun toDomain() = Person(isRegistered, username, email, isEmailConfirmed)
}
