package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class SceneRepository(private val apiRepository: SceneApiRepository) {

    class FlowableRefresherAndAdder(val flowable: Flowable<Result<List<Scene>>>, val refresher: Observer<Any>,
                                    val adder: Observer<Scene>)

    private val scenesStreamHashMap: HashMap<String, FlowableRefresherAndAdder> = HashMap()
    val adderObservable = PublishSubject.create<Scene>()
    val updaterObservable = PublishSubject.create<Scene>()

    fun scenesFlowable(experienceId: String): Flowable<Result<List<Scene>>> {
        if (scenesStreamHashMap.get(experienceId) == null) {
            val flowableRefresherPair = apiRepository.scenesFlowableAndRefreshObserver(experienceId)
            val cachedScenesFlowable =
                    Flowable.merge(
                            flowableRefresherPair.first
                                .map { Function<Result<List<Scene>>, Result<List<Scene>>> { _ -> it } },
                            adderObservable
                                    .toFlowable(BackpressureStrategy.LATEST)
                                    .map { t -> Function<Result<List<Scene>>, Result<List<Scene>>>
                                        { r ->
                                            val listWithNewElement = r.data!!.union(listOf(t))
                                            Result(listWithNewElement.toList(), null)
                                        }
                                    },
                            updaterObservable
                                    .toFlowable(BackpressureStrategy.LATEST)
                                    .map { t -> Function<Result<List<Scene>>, Result<List<Scene>>>
                                        { r ->
                                            val updatedList = r.data!!.filter { e -> e.id == t.id }.map { t }
                                            Result(updatedList, null)
                                        }
                                    }
                            )
                    .scan(Result(listOf<Scene>(), null), { oldValue, func -> func.apply(oldValue) })
                    .skip(1)
                    .replay(1)
                    .autoConnect()
            scenesStreamHashMap.put(experienceId,
                    FlowableRefresherAndAdder(flowable = cachedScenesFlowable,
                                              refresher = flowableRefresherPair.second,
                                              adder = adderObservable))
        }
        return scenesStreamHashMap.get(experienceId)!!.flowable
    }

    fun refreshScenes(experienceId: String) {
        scenesStreamHashMap.get(experienceId)!!.refresher.onNext(Any())
    }

    fun sceneFlowable(experienceId: String, sceneId: String): Flowable<Result<Scene>> =
        scenesFlowable(experienceId).map { Result(data = it.data?.first { it.id == sceneId }, error = it.error) }

    fun createScene(scene: Scene): Flowable<Result<Scene>> {
        return apiRepository.createScene(scene)
                .doOnNext { t -> adderObservable.onNext(t.data!!) }
    }

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        val delegate = { scene: Scene -> updaterObservable.onNext(scene)}
        apiRepository.uploadScenePicture(sceneId, croppedImageUriString, delegate)
    }
}
