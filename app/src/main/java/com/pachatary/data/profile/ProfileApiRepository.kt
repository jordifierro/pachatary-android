package com.pachatary.data.profile

import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import io.reactivex.Flowable
import io.reactivex.Scheduler
import retrofit2.Retrofit
import javax.inject.Named

class ProfileApiRepository(retrofit: Retrofit, @Named("io") val ioScheduler: Scheduler) {

    private val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)

    fun selfProfile(): Flowable<Result<Profile>> =
            profileApi.selfProfile()
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())

    fun profile(username: String): Flowable<Result<Profile>> =
            profileApi.profile(username)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())

    fun editProfile(bio: String): Flowable<Result<Profile>> =
            profileApi.editProfile(bio)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())
}
