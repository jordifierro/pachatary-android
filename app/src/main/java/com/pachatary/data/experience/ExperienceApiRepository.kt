package com.pachatary.data.experience

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.NetworkParserFactory
import com.pachatary.data.common.Result
import com.pachatary.data.picture.Picture
import io.reactivex.Flowable
import io.reactivex.Scheduler
import net.gotev.uploadservice.*
import org.json.JSONObject
import retrofit2.Retrofit
import javax.inject.Named

class ExperienceApiRepository (retrofit: Retrofit, @Named("io") val scheduler: Scheduler,
                               val context: Context, val authHttpInterceptor: AuthHttpInterceptor) {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    fun exploreExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.exploreExperiences()
                .compose(NetworkParserFactory.getListTransformer())
                .subscribeOn(scheduler)

    fun myExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.myExperiences()
                    .compose(NetworkParserFactory.getListTransformer())
                    .subscribeOn(scheduler)

    fun savedExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.savedExperiences()
                    .compose(NetworkParserFactory.getListTransformer())
                    .subscribeOn(scheduler)

    fun createExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.createExperience(title = experience.title, description = experience.description)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(scheduler)

    fun editExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.editExperience(experience.id, experience.title, experience.description)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(scheduler)

    fun saveExperience(save: Boolean, experienceId: String): Flowable<Result<Void>> {
        if (save) return experienceApi.saveExperience(experienceId)
                    .compose(NetworkParserFactory.getVoidTransformer())
                    .subscribeOn(scheduler)
        else return experienceApi.unsaveExperience(experienceId)
                    .compose(NetworkParserFactory.getVoidTransformer())
                    .subscribeOn(scheduler)
    }

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String,
                                delegate: (resultExperience: Result<Experience>) -> Unit) {
        try {
            val authHeader = authHttpInterceptor.getAuthHeader()
            MultipartUploadRequest(context,
                    BuildConfig.API_URL + "/experiences/" + experienceId + "/picture/")
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
                            val jsonExperience = JSONObject(serverResponse.bodyAsString)
                            delegate(Result(parseExperienceJson(jsonExperience)))
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
        val isMine = jsonExperience.getBoolean("is_mine")
        val isSaved = jsonExperience.getBoolean("is_saved")
        val authorUsername = jsonExperience.getString("author_username")

        return Experience(id = id, title = title, description = description,
                          picture = picture, isMine = isMine, isSaved = isSaved, author_username = authorUsername)
    }
}
