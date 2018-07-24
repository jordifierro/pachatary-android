package com.pachatary.data.auth

import com.pachatary.data.common.ClientException
import com.pachatary.data.common.Result
import io.reactivex.Flowable

class AuthRepository(val authStorageRepository: AuthStorageRepository,
                     private val authApiRepository: AuthApiRepository,
                     val currentVersion: Int) {

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

    fun canPersonCreateContent() = authStorageRepository.isRegistrationCompleted()

    fun register(username: String, email: String): Flowable<Result<Void>> =
            authApiRepository.register(username, email)
                    .doOnNext { if (it.isError() && (it.error is ClientException) &&
                                    it.error.code == "already_registered")
                                    authStorageRepository.setIsRegisterCompleted(true) }

    fun confirmEmail(confirmationToken: String): Flowable<Result<Void>> =
            authApiRepository.confirmEmail(confirmationToken)
                .doOnNext { if (it.isSuccess()) authStorageRepository.setIsRegisterCompleted(true) }

    fun askLoginEmail(email: String) = authApiRepository.askLoginEmail(email)

    fun login(loginToken: String): Flowable<Result<AuthToken>> =
            authApiRepository.login(loginToken)
                .doOnNext { if (it.isSuccess()) {
                                authStorageRepository.setIsRegisterCompleted(true)
                                authStorageRepository.setPersonCredentials(it.data!!)
                            } }

    fun currentVersionHasExpired(): Flowable<Boolean> =
            authApiRepository.clientVersions()
                    .filter { !it.isInProgress() }
                    .map { if (it.isSuccess()) currentVersion < it.data!!
                           else false }
}
