package com.pachatary.data.scene

import com.pachatary.data.common.NewResultStreamFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable

class SceneRepository(val apiRepository: SceneApiRepository, val streamFactory: NewResultStreamFactory<Scene>) {

    private val scenesStreamHashMap: HashMap<String, NewResultStreamFactory.ResultStream<Scene>> = HashMap()

    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesStreamHashMap.get(experienceId) == null) {
            val streams = streamFactory.create()
            scenesStreamHashMap.put(experienceId, streams)
            apiRepository.scenesRequestFlowable(experienceId)
                    .subscribe({ streams.addOrUpdateObserver.onNext(it.data!!) })
        }
        return scenesStreamHashMap.get(experienceId)!!.resultFlowable
    }

    fun sceneFlowable(experienceId: String, sceneId: String): Flowable<Result<Scene>> =
        scenesFlowable(experienceId).map { Result(data = it.data?.first { it.id == sceneId }, error = it.error) }

    fun createScene(scene: Scene): Flowable<Result<Scene>> {
        return apiRepository.createScene(scene).doOnNext(emitThroughAddOrUpdate)
    }

    fun editScene(scene: Scene): Flowable<Result<Scene>> {
        return apiRepository.editScene(scene).doOnNext(emitThroughAddOrUpdate)
    }

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        apiRepository.uploadScenePicture(sceneId, croppedImageUriString, emitThroughAddOrUpdate)
    }

    internal val emitThroughAddOrUpdate = { resultScene: Result<Scene> ->
        scenesStreamHashMap.get(resultScene.data!!.experienceId)!!.addOrUpdateObserver.onNext(
                listOf(resultScene.data)) }
}
