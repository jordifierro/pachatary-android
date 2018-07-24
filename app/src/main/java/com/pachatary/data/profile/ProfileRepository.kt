package com.pachatary.data.profile

import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class ProfileRepository(val apiRepository: ProfileApiRepository) {

    data class NotCachedProfileException(val unused: Unit = Unit) : Exception()

    private val profileSubject: PublishSubject<Profile>
    private var profilesFlowable: Flowable<List<Profile>>

    init {
        profileSubject = PublishSubject.create<Profile>()
        profilesFlowable = profileSubject.toFlowable(BackpressureStrategy.LATEST)
                .scan(listOf()) { oldProfiles: List<Profile>, newProfile: Profile ->
                    val newProfiles = oldProfiles.filter {
                        it.username != newProfile.username }.toMutableList()
                    newProfiles.add(newProfile)
                    newProfiles.toList()
                }
                .distinct()
                .replay(1)
                .autoConnect()
        val startConnectionDisposable = profilesFlowable.subscribe()
        startConnectionDisposable.dispose()
    }

    fun profile(username: String): Flowable<Result<Profile>> =
            profilesFlowable.map { it.filter { it.username == username } }
                    .map { if (it.isEmpty()) ResultError(NotCachedProfileException())
                           else ResultSuccess(it[0]) }
                    .flatMap { if (it.isError()) apiRepository.profile(username)
                                .doOnNext { if (it.isSuccess()) this.cacheProfile(it.data!!) }
                               else Flowable.just(it) }
                    .distinct()

    fun selfProfile(): Flowable<Result<Profile>> =
            profilesFlowable.map { it.filter { it.isMe } }
                    .map { if (it.isEmpty()) ResultError(NotCachedProfileException())
                           else ResultSuccess(it[0]) }
                    .flatMap { if (it.isError()) apiRepository.selfProfile()
                                .doOnNext { if (it.isSuccess()) this.cacheProfile(it.data!!) }
                               else Flowable.just(it) }
                    .distinct()

    fun editProfile(bio: String): Flowable<Result<Profile>> =
            apiRepository.editProfile(bio)
                    .doOnNext { if (it.isSuccess()) this.cacheProfile(it.data!!) }

    fun cacheProfile(profile: Profile) {
        (profileSubject as Observer<Profile>).onNext(profile)
    }

    fun uploadProfilePicture(imageUriString: String) {
        apiRepository.uploadProfilePicture(imageUriString)
                .doOnNext { if (it.isSuccess()) this.cacheProfile(it.data!!) }
                .subscribe()
    }
}
