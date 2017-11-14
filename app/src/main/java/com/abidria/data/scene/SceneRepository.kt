package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function

class SceneRepository(private val apiRepository: SceneApiRepository) {

    private val scenesStreamHashMap: HashMap<String, Pair<Flowable<Result<List<Scene>>>, Observer<Any>>> = HashMap()

    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesStreamHashMap.get(experienceId) == null) {
            val flowableRefresherPair = apiRepository.scenesFlowableAndRefreshObserver(experienceId)
            val cachedScenesFlowable = flowableRefresherPair.first
                    .map { Function<Result<List<Scene>>, Result<List<Scene>>> { _ -> it } }
                    .scan(Result(listOf<Scene>(), null), { oldValue, func -> func.apply(oldValue) })
                    .skip(1)
                    .replay(1)
                    .autoConnect()
            scenesStreamHashMap.put(experienceId,
                    Pair(first = cachedScenesFlowable, second = flowableRefresherPair.second))
        }
        return scenesStreamHashMap.get(experienceId)!!.first
    }

    fun refreshScenes(experienceId: String) {
        scenesStreamHashMap.get(experienceId)!!.second.onNext(Any())
    }

    fun sceneFlowable(experienceId: String, sceneId: String): Flowable<Result<Scene>> =
        scenesFlowable(experienceId).map { Result(data = it.data?.first { it.id == sceneId }, error = it.error) }

    fun createScene(scene: Scene) = apiRepository.createScene(scene)

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        apiRepository.uploadScenePicture(sceneId, croppedImageUriString)
    }
}
