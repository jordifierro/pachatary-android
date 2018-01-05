package com.pachatary.data.scene

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import com.pachatary.data.picture.Picture
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import net.gotev.uploadservice.*
import org.json.JSONObject
import retrofit2.Retrofit
import javax.inject.Named


class SceneApiRepository(retrofit: Retrofit, @Named("io") val scheduler: Scheduler,
                         val context: Context, val authHttpInterceptor: AuthHttpInterceptor) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun scenesRequestFlowable(experienceId: String): Flowable<Result<List<Scene>>> =
        PublishSubject.create<Any>().startWith(Any())
                            .flatMap { sceneApi.scenes(experienceId).subscribeOn(scheduler).toObservable() }
                            .toFlowable(BackpressureStrategy.LATEST)
                            .compose(NetworkParserFactory.getListTransformer())

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
                            val jsonScene = JSONObject(serverResponse.bodyAsString)
                            delegate(Result(parseSceneJson(jsonScene), null))
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
        }
    }

    private fun parseSceneJson(jsonScene: JSONObject): Scene {
        val id = jsonScene.getString("id")
        val title = jsonScene.getString("title")
        val description = jsonScene.getString("description")
        val latitude = jsonScene.getDouble("latitude")
        val longitude = jsonScene.getDouble("longitude")
        val experienceId = jsonScene.getString("experience_id")
        val pictureJson = jsonScene.getJSONObject("picture")
        val smallUrl = pictureJson.getString("small_url")
        val mediumUrl = pictureJson.getString("medium_url")
        val largeUrl = pictureJson.getString("large_url")
        val picture = Picture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)

        return Scene(id = id, title = title, description = description, latitude = latitude,
                     longitude = longitude, experienceId = experienceId, picture = picture)
    }
}
