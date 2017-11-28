package com.abidria.data.experience

import android.content.Context
import android.net.Uri
import android.util.Log
import com.abidria.BuildConfig
import com.abidria.data.common.ParseNetworkResultTransformer
import com.abidria.data.common.Result
import com.abidria.data.picture.Picture
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import net.gotev.uploadservice.*
import org.json.JSONObject
import retrofit2.Retrofit
import javax.inject.Named

class ExperienceApiRepository (retrofit: Retrofit, @Named("io") val scheduler: Scheduler, val context: Context) {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    fun experiencesFlowable(): Flowable<Result<List<Experience>>> =
        PublishSubject.create<Any>()
            .startWith(true)
            .flatMap { experienceApi.experiences().subscribeOn(scheduler).toObservable() }
            .toFlowable(BackpressureStrategy.LATEST)
            .compose(ParseNetworkResultTransformer({ it.map { it.toDomain() } }))

    fun createExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.createExperience(title = experience.title, description = experience.description)
                    .compose(ParseNetworkResultTransformer({ it.toDomain() }))

    fun editExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.editExperience(experience.id, experience.title, experience.description)
                    .compose(ParseNetworkResultTransformer({ it.toDomain() }))

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String,
                                delegate: (resultExperience: Result<Experience>) -> Unit) {
        try {
            val uploadId = MultipartUploadRequest(context,
                    BuildConfig.API_URL + "/experiences/" + experienceId + "/picture/")
                    .addFileToUpload(Uri.parse(croppedImageUriString).path, "picture")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(object : UploadStatusDelegate {
                        override fun onProgress(context: Context, uploadInfo: UploadInfo) {}
                        override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse,
                                             exception: Exception) {}
                        override fun onCancelled(context: Context, uploadInfo: UploadInfo) {}
                        override fun onCompleted(context: Context, uploadInfo: UploadInfo,
                                                 serverResponse: ServerResponse) {
                            val jsonExperience = JSONObject(serverResponse.bodyAsString)
                            delegate(Result(parseExperienceJson(jsonExperience), null))
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
        }
    }

    private fun parseExperienceJson(jsonExperience: JSONObject): Experience {
        val id = jsonExperience.getString("id")
        val title = jsonExperience.getString("title")
        val description = jsonExperience.getString("description")
        val pictureJson = jsonExperience.getJSONObject("picture")
        val smallUrl = pictureJson.getString("small_url")
        val mediumUrl = pictureJson.getString("medium_url")
        val largeUrl = pictureJson.getString("large_url")
        val picture = Picture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)

        return Experience(id = id, title = title, description = description, picture = picture)
    }
}
