package com.pachatary.data

import com.pachatary.data.common.ClientException
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.data.scene.Scene


fun <T> DummyResultError(): Result<T> = ResultError(ClientException("", "", ""))

fun DummyScene(id: String, experienceId: String = "") =
    Scene(id = id, title = "", description = "", picture = null,
          latitude = 1.2, longitude = -3.4, experienceId = experienceId)

fun DummySceneResultSuccess(id: String, experienceId: String = "") =
        ResultSuccess(DummyScene(id, experienceId))

fun DummyScenesResultSuccess(vararg ids: String): Result<List<Scene>> {
    val scenes = mutableListOf<Scene>()
    ids.forEach { id -> scenes.add(DummyScene(id)) }
    return ResultSuccess(scenes)
}

fun DummyProfile(username: String, bio: String = "", isMe: Boolean = false) =
        Profile(username = username, bio = bio, picture = null, isMe = isMe)

fun DummyProfileResult(username: String, bio: String = "", isMe: Boolean = false) =
        ResultSuccess(DummyProfile(username, bio, isMe))

fun DummyExperience(id: String, username: String = "") =
    Experience(id = id, title = "", description = "", picture = null,
              isMine = false, isSaved = false, savesCount = 0,
              authorProfile = DummyProfile(username))

fun DummyExperienceResultSuccess(id: String, username: String = "") =
        ResultSuccess(DummyExperience(id, username))

fun DummyExperiencesResultSuccess(ids: List<String>, usernames: List<String>? = null)
                                                                        : Result<List<Experience>> {
    val experiences = mutableListOf<Experience>()
    for (i in 0..ids.size-1) {
        if (usernames != null) experiences.add(DummyExperience(ids[i], usernames[i]))
        else experiences.add(DummyExperience(ids[i]))
    }
    return ResultSuccess(experiences)
}
