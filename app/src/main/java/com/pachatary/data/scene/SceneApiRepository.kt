package com.pachatary.data.scene

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.*
import com.pachatary.data.picture.BigPicture
import io.reactivex.*
import net.gotev.uploadservice.*
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Named


class SceneApiRepository(retrofit: Retrofit, @Named("io") private val ioScheduler: Scheduler,
                         private val imageUploader: ImageUploader) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun scenesRequestFlowable(experienceId: String): Flowable<Result<List<Scene>>> =
        sceneApi.scenes(experienceId)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getListTransformer())
                .startWith(ResultInProgress())

    fun createScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.createScene(title = scene.title, description = scene.description,
                             latitude = scene.latitude, longitude = scene.longitude,
                             experienceId = scene.experienceId)
                .compose(NetworkParserFactory.getTransformer())

    fun editScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.editScene(scene.id, scene.title, scene.description,
                           scene.latitude, scene.longitude, scene.experienceId)
                .compose(NetworkParserFactory.getTransformer())

    fun uploadScenePicture(sceneId: String, imageUriString: String): Flowable<Result<Scene>> =
            imageUploader.upload(imageUriString, "/scenes/$sceneId/picture/")
                    .subscribeOn(ioScheduler)
                    .map {
                        when {
                            it.isInProgress() -> ResultInProgress()
                            it.isError() -> ResultError(it.error!!)
                            else -> ResultSuccess(parseSceneJson(it.data!!))
                        }
                    }

    internal fun parseSceneJson(jsonScene: JsonObject): Scene {
        val id = jsonScene.get("id").asString
        val title = jsonScene.get("title").asString
        val description = jsonScene.get("description").asString
        val latitude = jsonScene.get("latitude").asDouble
        val longitude = jsonScene.get("longitude").asDouble
        val experienceId = jsonScene.get("experience_id").asString
        val pictureJson = jsonScene.get("picture").asJsonObject
        val smallUrl = pictureJson.get("small_url").asString
        val mediumUrl = pictureJson.get("medium_url").asString
        val largeUrl = pictureJson.get("large_url").asString
        val picture = BigPicture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)

        return Scene(id = id, title = title, description = description, latitude = latitude,
                     longitude = longitude, experienceId = experienceId, picture = picture)
    }
}
