package com.pachatary.data.scene

import android.annotation.SuppressLint
import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.common.Result
import io.reactivex.Flowable

class SceneRepository(val apiRepository: SceneApiRepository, val cacheFactory: ResultCacheFactory<Scene>) {

    private val scenesCacheHashMap: HashMap<String, ResultCacheFactory.ResultCache<Scene>> = HashMap()

    @SuppressLint("CheckResult")
    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesCacheHashMap.get(experienceId) == null) {
            val cache = cacheFactory.create()
            scenesCacheHashMap.put(experienceId, cache)
            apiRepository.scenesRequestFlowable(experienceId)
                    .subscribe({ cache.addOrUpdateObserver.onNext(it.data!!) }, { throw it })
        }
        return scenesCacheHashMap.get(experienceId)!!.resultFlowable
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
        scenesCacheHashMap.get(resultScene.data!!.experienceId)!!.addOrUpdateObserver.onNext(
                listOf(resultScene.data)) }
}
