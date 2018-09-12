package com.pachatary.data.scene

import android.annotation.SuppressLint
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultCacheFactory
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.Status
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction

class SceneRepository(val apiRepository: SceneApiRepository,
                      private val cacheFactory: ResultCacheFactory<Scene>) {

    private val scenesCacheHashMap: HashMap<String, ResultCacheFactory.ResultCache<Scene>> = HashMap()

    @SuppressLint("CheckResult")
    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesCacheHashMap[experienceId] == null) {
            val cache = cacheFactory.create()
            scenesCacheHashMap[experienceId] = cache
            requestScenes(experienceId)
            return scenesCacheHashMap[experienceId]!!.resultFlowable
        }
        else {
            return scenesCacheHashMap[experienceId]!!.resultFlowable
                    .zipWith(Flowable.range(0, Int.MAX_VALUE),
                             BiFunction<Result<List<Scene>>, Int, Pair<Int, Result<List<Scene>>>>
                             { result, index -> Pair(index, result) })
                    .filter { if (it.first == 0 && it.second.isError()) {
                                  requestScenes(experienceId)
                                  false
                              } else true }
                    .map { it.second }
        }
    }

    fun refreshScenes(experienceId: String) {
        if (scenesCacheHashMap[experienceId] != null) {
            scenesCacheHashMap[experienceId]!!.replaceResultObserver.onNext(ResultInProgress())
            requestScenes(experienceId)
        }
    }

    @SuppressLint("CheckResult")
    private fun requestScenes(experienceId: String) {
        apiRepository.scenesRequestFlowable(experienceId)
                .subscribe({ scenesCacheHashMap[experienceId]!!
                        .replaceResultObserver.onNext(it)}, { throw it })
    }

    fun sceneFlowable(experienceId: String, sceneId: String): Flowable<Result<Scene>> =
        scenesFlowable(experienceId).map {
            Result(it.status, data = it.data!!.first { scene: Scene -> scene.id == sceneId },
                   error = it.error)
        }

    fun createScene(scene: Scene): Flowable<Result<Scene>> =
        apiRepository.createScene(scene)
                .doOnNext(emitThroughAddOrUpdate)

    fun editScene(scene: Scene): Flowable<Result<Scene>> =
        apiRepository.editScene(scene)
                .doOnNext(emitThroughUpdate)

    @SuppressLint("CheckResult")
    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        apiRepository.uploadScenePicture(sceneId, croppedImageUriString)
                .doOnNext(emitThroughUpdate)
                .subscribe({}, { throw it } )
    }

    private val emitThroughAddOrUpdate =
            { resultScene: Result<Scene> ->
                if (resultScene.status == Status.SUCCESS) {
                    scenesCacheHashMap[resultScene.data!!.experienceId]!!
                            .addOrUpdateObserver.onNext(Pair(listOf(resultScene.data),
                                                             ResultCacheFactory.AddPosition.END))
                }
            }

    private val emitThroughUpdate =
            { resultScene: Result<Scene> ->
                if (resultScene.status == Status.SUCCESS) {
                    scenesCacheHashMap[resultScene.data!!.experienceId]!!
                            .updateObserver.onNext(listOf(resultScene.data))
                }
            }
}
