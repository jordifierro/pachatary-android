package com.pachatary.data

import com.pachatary.data.auth.ClientException
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.scene.Scene

fun ScenesListResultSuccess(vararg ids: String): Result<List<Scene>> {
    val scenes = mutableListOf<Scene>()
    ids.forEach { id -> scenes.add(DummyScene(id)) }
    return ResultSuccess(scenes)
}

fun SceneResultSuccess(id: String, experienceId: String = ""): Result<Scene> {
    return ResultSuccess(DummyScene(id, experienceId))
}

fun <T> DummyResultError(): Result<T> {
    return ResultError(ClientException("", "", ""))
}

fun DummyScene(id: String, experienceId: String = ""): Scene {
    return Scene(id = id, title = "", description = "", picture = null,
                 latitude = 1.2, longitude = -3.4, experienceId = experienceId)

}
