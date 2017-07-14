package com.abidria.data.experience

import com.abidria.data.common.ParseNetworkResultTransformer
import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit
import javax.inject.Named

class ExperienceApiRepository (retrofit: Retrofit, @Named("io") val scheduler: Scheduler) {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    private val publisher: PublishSubject<Any> = PublishSubject.create<Any>()

    fun experiencesFlowable(): Flowable<Result<List<Experience>>> =
        publisher
            .startWith(true)
            .flatMap { experienceApi.experiences().subscribeOn(scheduler).toObservable() }
            .toFlowable(BackpressureStrategy.LATEST)
            .compose(ParseNetworkResultTransformer({ it.map { mapper -> mapper.toDomain() } }))

    fun refreshExperiences() {
        publisher.onNext(Any())
    }
}
