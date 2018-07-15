package com.pachatary.data.experience

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.*
import com.pachatary.data.picture.BigPicture
import com.pachatary.data.picture.LittlePicture
import com.pachatary.data.profile.Profile
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import net.gotev.uploadservice.*
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Named

class ExperienceApiRepository (retrofit: Retrofit, @Named("io") val scheduler: Scheduler,
                               val context: Context, val authHttpInterceptor: AuthHttpInterceptor) {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    fun exploreExperiencesFlowable(word: String?, latitude: Double?, longitude: Double?)
            : Flowable<Result<List<Experience>>> =
            experienceApi.exploreExperiences(word, latitude, longitude)
                .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                .subscribeOn(scheduler)

    fun myExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.myExperiences()
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(scheduler)

    fun savedExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.savedExperiences()
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(scheduler)

    fun personsExperienceFlowable(username: String): Flowable<Result<List<Experience>>> =
            experienceApi.personsExperiences(username)
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(scheduler)

    fun paginateExperiences(url: String): Flowable<Result<List<Experience>>> =
            experienceApi.paginateExperiences(url)
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(scheduler)

    fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
            experienceApi.getExperience(experienceId)
                    .subscribeOn(scheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())

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

    fun translateShareId(experienceShareId: String): Flowable<Result<String>> =
            experienceApi.translateShareId(experienceShareId)
                    .subscribeOn(scheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())

    fun uploadExperiencePicture(experienceId: String, croppedImageUriString: String)
                                                                    : Flowable<Result<Experience>> =
        Flowable.create<Result<Experience>>({ emitter ->
            try {
                val authHeader = authHttpInterceptor.getAuthHeader()
                MultipartUploadRequest(context,
                        BuildConfig.API_URL + "/experiences/" + experienceId + "/picture/")
                        .addFileToUpload(Uri.parse(croppedImageUriString).path, "picture")
                        .setNotificationConfig(UploadNotificationConfig())
                        .setMaxRetries(3)
                        .addHeader(authHeader.key, authHeader.value)
                        .setDelegate(object : UploadStatusDelegate {
                            override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                                emitter.onComplete()
                            }

                            override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {}

                            override fun onError(context: Context?, uploadInfo: UploadInfo?,
                                                 serverResponse: ServerResponse?,
                                                 exception: java.lang.Exception?) {
                                if (exception is UnknownHostException) {
                                    emitter.onNext(ResultError(exception))
                                    emitter.onComplete()
                                }
                                else emitter.onError(exception!!)
                            }

                            override fun onCompleted(context: Context?, uploadInfo: UploadInfo?,
                                                     serverResponse: ServerResponse?) {
                                val jsonExperience = JsonParser().parse(
                                        serverResponse!!.bodyAsString).asJsonObject
                                emitter.onNext(ResultSuccess(parseExperienceJson(jsonExperience)))
                                emitter.onComplete()
                            }
                        })
                        .startUpload()
            } catch (exc: Exception) {
                emitter.onError(exc)
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(scheduler)
                .startWith(ResultInProgress())

    internal fun parseExperienceJson(jsonExperience: JsonObject): Experience {
        val id = jsonExperience.get("id").asString
        val title = jsonExperience.get("title").asString
        val description = jsonExperience.get("description").asString
        val pictureJson = jsonExperience.get("picture").asJsonObject
        val smallUrl = pictureJson.get("small_url").asString
        val mediumUrl = pictureJson.get("medium_url").asString
        val largeUrl = pictureJson.get("large_url").asString
        val picture = BigPicture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)
        val isMine = jsonExperience.get("is_mine").asBoolean
        val isSaved = jsonExperience.get("is_saved").asBoolean
        val authorProfileJson = jsonExperience.get("author_profile").asJsonObject
        val username = authorProfileJson.get("username").asString
        val bio = authorProfileJson.get("bio").asString
        val isMe = authorProfileJson.get("is_me").asBoolean
        var profilePicture: LittlePicture? = null
        if (!authorProfileJson.get("picture").isJsonNull) {
            val profilePictureJson = authorProfileJson.get("picture").asJsonObject
            val profilePictureTinyUrl = profilePictureJson.get("tiny_url").asString
            val profilePictureSmallUrl = profilePictureJson.get("small_url").asString
            val profilePictureMediumUrl = profilePictureJson.get("medium_url").asString
            profilePicture = LittlePicture(tinyUrl = profilePictureTinyUrl,
                                           smallUrl = profilePictureSmallUrl,
                                           mediumUrl = profilePictureMediumUrl)
        }
        val authorProfile = Profile(username = username, bio = bio,
                                    picture = profilePicture, isMe = isMe)
        val savesCount = jsonExperience.get("saves_count").asInt

        return Experience(id = id, title = title, description = description,
                          picture = picture, isMine = isMine, isSaved = isSaved,
                          authorProfile = authorProfile, savesCount = savesCount)
    }
}
