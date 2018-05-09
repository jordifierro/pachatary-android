package com.pachatary.data.auth

import com.pachatary.data.common.Result
import io.reactivex.Flowable

class AuthRepository(val authStorageRepository: AuthStorageRepository,
                     private val authApiRepository: AuthApiRepository) {

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
        return try { authStorageRepository.getPerson().isEmailConfirmed }
        catch (e: NoPersonInfoException) { false }
    }

    fun register(username: String, email: String): Flowable<Result<Person>> =
            authApiRepository.register(username, email)
                .doOnNext { if (it.isSuccess()) savePerson(it.data!!) }

    fun confirmEmail(confirmationToken: String): Flowable<Result<Person>> =
            authApiRepository.confirmEmail(confirmationToken)
                .doOnNext { if (it.isSuccess()) savePerson(it.data!!) }

    fun askLoginEmail(email: String) = authApiRepository.askLoginEmail(email)

    fun login(loginToken: String): Flowable<Result<Pair<Person, AuthToken>>> =
            authApiRepository.login(loginToken)
                .doOnNext { if (it.isSuccess()) {
                                savePerson(it.data!!.first)
                                authStorageRepository.setPersonCredentials(it.data.second)
                            } }

    internal fun savePerson(person: Person) {
        authStorageRepository.setPerson(person)
    }
}
