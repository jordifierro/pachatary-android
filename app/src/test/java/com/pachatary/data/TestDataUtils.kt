package com.pachatary.data

import com.pachatary.data.auth.ClientException
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.experience.Experience
import com.pachatary.data.scene.Scene

fun ScenesListResultSuccess(vararg ids: String): Result<List<Scene>> {
    val scenes = mutableListOf<Scene>()
    ids.forEach { id -> scenes.add(DummyScene(id)) }
    return ResultSuccess(scenes)
}

fun SceneResultSuccess(id: String, experienceId: String = "") =
        ResultSuccess(DummyScene(id, experienceId))

fun <T> DummyResultError(): Result<T> = ResultError(ClientException("", "", ""))

fun DummyScene(id: String, experienceId: String = "") =
    Scene(id = id, title = "", description = "", picture = null,
          latitude = 1.2, longitude = -3.4, experienceId = experienceId)

fun DummyExperience(id: String) =
        Experience(id = id, title = "", description = "", picture = null,
                    isMine = false, isSaved = false, authorUsername = "", savesCount = 0)

fun ExperienceResultSuccess(id: String): Result<Experience> = ResultSuccess(DummyExperience(id))
