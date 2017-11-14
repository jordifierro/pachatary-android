package com.abidria.data.scene

import android.content.Context
import android.net.Uri
import android.util.Log
import com.abidria.BuildConfig
import com.abidria.data.common.ParseNetworkResultTransformer
import com.abidria.data.common.Result
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import io.reactivex.subjects.PublishSubject
import net.gotev.uploadservice.MultipartUploadRequest
import net.gotev.uploadservice.UploadNotificationConfig
import retrofit2.Retrofit
import javax.inject.Named

class SceneApiRepository(retrofit: Retrofit, @Named("io") val scheduler: Scheduler, val context: Context) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun scenesFlowableAndRefreshObserver(experienceId: String):
            Pair<Flowable<Result<List<Scene>>>, Observer<Any>> {
        val refreshPublisher: PublishSubject<Any> = PublishSubject.create()
        return Pair(first = refreshPublisher.startWith(Any())
                            .flatMap { sceneApi.scenes(experienceId).subscribeOn(scheduler).toObservable() }
                            .toFlowable(BackpressureStrategy.LATEST)
                            .compose<Result<List<Scene>>>(ParseNetworkResultTransformer({ it.map { it.toDomain() } })),
                    second = refreshPublisher)
    }

    fun createScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.createScene(title = scene.title, description = scene.description,
                             latitude = scene.latitude, longitude = scene.longitude,
                             experienceId = scene.experienceId)
                .compose(ParseNetworkResultTransformer({ it.toDomain() }))

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String) {
        try {
            val uploadId = MultipartUploadRequest(context,
                    BuildConfig.API_URL + "/scenes/" + sceneId + "/picture/")
                    .addFileToUpload(Uri.parse(croppedImageUriString).path, "picture")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload()
        } catch (exc: Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
        }
    }

}
