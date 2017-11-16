package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.Flowable

class SceneRepository(val apiRepository: SceneApiRepository, val streamFactory: SceneStreamFactory) {

    private val scenesStreamHashMap: HashMap<String, SceneStreamFactory.ScenesStream> = HashMap()

    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesStreamHashMap.get(experienceId) == null) {
            val streams = streamFactory.create()
            scenesStreamHashMap.put(experienceId, streams)
            apiRepository.scenesRequestFlowable(experienceId).subscribe({ streams.replaceAllScenesObserver.onNext(it) })
        }
        return scenesStreamHashMap.get(experienceId)!!.scenesFlowable
    }

    fun sceneFlowable(experienceId: String, sceneId: String): Flowable<Result<Scene>> =
        scenesFlowable(experienceId).map { Result(data = it.data?.first { it.id == sceneId }, error = it.error) }

    fun createScene(scene: Scene): Flowable<Result<Scene>> {
        return apiRepository.createScene(scene)
                .doOnNext { t -> scenesStreamHashMap.get(scene.experienceId)!!.addOrUpdateSceneObserver.onNext(t) }
    }

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        val delegate = { scene: Scene ->
            scenesStreamHashMap.get(scene.experienceId)!!.addOrUpdateSceneObserver.onNext(Result(scene, null))}
        apiRepository.uploadScenePicture(sceneId, croppedImageUriString, delegate)
    }
}
