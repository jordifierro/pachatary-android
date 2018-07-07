package com.pachatary.data.scene

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import com.pachatary.data.picture.Picture
import io.reactivex.Flowable
import io.reactivex.Scheduler
import net.gotev.uploadservice.*
import retrofit2.Retrofit
import javax.inject.Named


class SceneApiRepository(retrofit: Retrofit, @Named("io") val ioScheduler: Scheduler,
                         val context: Context, val authHttpInterceptor: AuthHttpInterceptor) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun scenesRequestFlowable(experienceId: String): Flowable<Result<List<Scene>>> =
        sceneApi.scenes(experienceId)
                .subscribeOn(ioScheduler)
                .compose(NetworkParserFactory.getListTransformer())
                .startWith(Result(listOf(), inProgress = true))

    fun createScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.createScene(title = scene.title, description = scene.description,
                             latitude = scene.latitude, longitude = scene.longitude,
                             experienceId = scene.experienceId)
                .compose(NetworkParserFactory.getTransformer())

    fun editScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.editScene(scene.id, scene.title, scene.description,
                           scene.latitude, scene.longitude, scene.experienceId)
                .compose(NetworkParserFactory.getTransformer())

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String,
                           delegate: (resultScene: Result<Scene>) -> Unit) {
        try {
            val authHeader = authHttpInterceptor.getAuthHeader()
            MultipartUploadRequest(context,
                    BuildConfig.API_URL + "/scenes/" + sceneId + "/picture/")
                    .addFileToUpload(Uri.parse(croppedImageUriString).path, "picture")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .addHeader(authHeader.key, authHeader.value)
                    .setDelegate(object : UploadStatusDelegate {
                        override fun onProgress(context: Context, uploadInfo: UploadInfo) {}
                        override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse,
                                             exception: Exception) {}
                        override fun onCancelled(context: Context, uploadInfo: UploadInfo) {}
                        override fun onCompleted(context: Context, uploadInfo: UploadInfo,
                                                 serverResponse: ServerResponse) {
                            val jsonScene =
                                    JsonParser().parse(serverResponse.bodyAsString).asJsonObject
                            delegate(Result(parseSceneJson(jsonScene)))
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
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
        val picture = Picture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)

        return Scene(id = id, title = title, description = description, latitude = latitude,
                     longitude = longitude, experienceId = experienceId, picture = picture)
    }
}
