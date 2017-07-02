package com.abidria.data.experience

import io.reactivex.Flowable
import retrofit2.Retrofit

class ExperienceRepository(retrofit: Retrofit) {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    fun getExperiences(): Flowable<List<Experience>> = experienceApi.experiences()
                                                                    .flatMapIterable { list -> list }
                                                                    .map { it.toDomain() }
                                                                    .toList()
                                                                    .toFlowable()

    fun getExperience(experienceId: String): Flowable<Experience> =
            getExperiences().flatMapIterable { list -> list }
                            .filter { experience -> experience.id == experienceId }
}
