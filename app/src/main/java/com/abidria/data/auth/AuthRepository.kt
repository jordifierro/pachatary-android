package com.abidria.data.auth

import com.abidria.data.common.Result
import io.reactivex.Flowable

class AuthRepository(val authStorageRepository: AuthStorageRepository, val authApiRepository: AuthApiRepository) {

    fun hasPersonCredentials(): Boolean {
        try {
            authStorageRepository.getPersonCredentials()
            return true
        } catch (e: NoLoggedException) {
            return false
        }
    }

    fun getPersonInvitation(): Flowable<Result<AuthToken>> {
        return authApiRepository.getPersonInvitation()
                .doOnNext { authStorageRepository.setPersonCredentials(it.data!!) }
    }

    fun savePerson(person: Person) {
        authStorageRepository.setPerson(person)
    }

    fun canPersonCreateContent(): Boolean {
        try {
            return authStorageRepository.getPerson().isEmailConfirmed
        } catch (e: NoPersonInfoException) {
            return false
        }
    }
}
