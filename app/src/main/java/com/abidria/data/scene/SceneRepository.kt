package com.abidria.data.scene

import io.reactivex.Flowable
import retrofit2.Retrofit

class SceneRepository(retrofit: Retrofit) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun getScenes(experienceId: String): Flowable<List<Scene>> = sceneApi.scenes(experienceId)
                                                                         .flatMapIterable { list -> list }
                                                                         .map { it.toDomain() }
                                                                         .toList()
                                                                         .toFlowable()
                                                                         .retry(2)

    fun getScene(experienceId: String, sceneId: String): Flowable<Scene> =
            getScenes(experienceId).flatMapIterable { list -> list }
                                   .filter { scene -> scene.id == sceneId }
}
