package com.pachatary.data.auth

import com.pachatary.data.common.Result
import io.reactivex.Flowable

class AuthRepository(val authStorageRepository: AuthStorageRepository, val authApiRepository: AuthApiRepository) {

    fun hasPersonCredentials(): Boolean {
        return try { authStorageRepository.getPersonCredentials()
            true }
        catch (e: NoLoggedException) { false }
    }

    fun getPersonInvitation(): Flowable<Result<AuthToken>> {
        return authApiRepository.getPersonInvitation()
                .doOnNext { if (it.isSuccess())
                                authStorageRepository.setPersonCredentials(it.data!!) }
    }

    fun canPersonCreateContent(): Boolean {
        try { return authStorageRepository.getPerson().isEmailConfirmed }
        catch (e: NoPersonInfoException) { return false }
    }

    fun register(username: String, email: String) = authApiRepository.register(username, email)
                .doOnNext { if (it.isSuccess()) savePerson(it.data!!) }

    fun confirmEmail(confirmationToken: String) = authApiRepository.confirmEmail(confirmationToken)
                .doOnNext { if (it.isSuccess()) savePerson(it.data!!) }

    internal fun savePerson(person: Person) {
        authStorageRepository.setPerson(person)
    }
}
