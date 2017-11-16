package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject

class SceneStreamFactory {

    data class ScenesStream(val replaceAllScenesObserver: Observer<Result<List<Scene>>>,
                            val addOrUpdateSceneObserver: Observer<Result<Scene>>,
                            val scenesFlowable: Flowable<Result<List<Scene>>>)

    fun create(): ScenesStream {
        val replaceAllScenesSubject = PublishSubject.create<Result<List<Scene>>>()
        val addOrUpdateSceneSubject = PublishSubject.create<Result<Scene>>()
        val scenesFlowable = Flowable.merge(
                replaceAllScenesSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { Function<Result<List<Scene>>, Result<List<Scene>>> { _ -> it } },
                addOrUpdateSceneSubject.toFlowable(BackpressureStrategy.LATEST)
                        .map { newSceneResult -> Function<Result<List<Scene>>, Result<List<Scene>>>
                                { previousSceneListResult ->
                                    if (previousSceneListResult.data!!
                                            .filter { it.id == newSceneResult.data!!.id }.size > 0) {
                                        val updatedSceneList = previousSceneListResult.data
                                                .map { scene ->
                                                    if (scene.id == newSceneResult.data!!.id) newSceneResult.data
                                                    else scene
                                                }
                                        Result(updatedSceneList.toList(), null)
                                    }
                                    else {
                                        val updatedScenesSet =
                                                previousSceneListResult.data.union(listOf(newSceneResult.data!!))
                                        Result(updatedScenesSet.toList(), null)
                                    }
                                }
                             }
                        )
                        .scan(Result(listOf<Scene>(), null), { oldValue, func -> func.apply(oldValue) })
                        .skip(1)
                        .replay(1)
                        .autoConnect()
        return ScenesStream(replaceAllScenesSubject, addOrUpdateSceneSubject, scenesFlowable)
    }
}
