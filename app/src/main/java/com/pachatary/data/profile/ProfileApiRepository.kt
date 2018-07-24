package com.pachatary.data.profile

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.*
import com.pachatary.data.picture.LittlePicture
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import net.gotev.uploadservice.*
import retrofit2.Retrofit
import java.net.UnknownHostException
import javax.inject.Named

class ProfileApiRepository(retrofit: Retrofit, @Named("io") val ioScheduler: Scheduler,
                           val authHttpInterceptor: AuthHttpInterceptor,
                           val context: Context) {

    private val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)

    fun selfProfile(): Flowable<Result<Profile>> =
            profileApi.selfProfile()
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())

    fun profile(username: String): Flowable<Result<Profile>> =
            profileApi.profile(username)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())

    fun editProfile(bio: String): Flowable<Result<Profile>> =
            profileApi.editProfile(bio)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)
                    .startWith(ResultInProgress())

    fun uploadProfilePicture(imageUriString: String): Flowable<Result<Profile>> =
        Flowable.create<Result<Profile>>({ emitter ->
            try {
                val authHeader = authHttpInterceptor.getAuthHeader()
                MultipartUploadRequest(context,
                        BuildConfig.API_URL + "/profiles/me/picture/")
                        .addFileToUpload(Uri.parse(imageUriString).path, "picture")
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
                                val jsonProfile = JsonParser().parse(
                                        serverResponse!!.bodyAsString).asJsonObject
                                emitter.onNext(ResultSuccess(parseProfileJson(jsonProfile)))
                                emitter.onComplete()
                            }
                        })
                        .startUpload()
            } catch (exc: Exception) {
                emitter.onError(exc)
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(ioScheduler)
                .startWith(ResultInProgress())

    internal fun parseProfileJson(jsonProfile: JsonObject): Profile {
        val username = jsonProfile.get("username").asString
        val bio = jsonProfile.get("bio").asString
        val isMe = jsonProfile.get("is_me").asBoolean
        var profilePicture: LittlePicture? = null
        if (!jsonProfile.get("picture").isJsonNull) {
            val profilePictureJson = jsonProfile.get("picture").asJsonObject
            val profilePictureTinyUrl = profilePictureJson.get("tiny_url").asString
            val profilePictureSmallUrl = profilePictureJson.get("small_url").asString
            val profilePictureMediumUrl = profilePictureJson.get("medium_url").asString
            profilePicture = LittlePicture(tinyUrl = profilePictureTinyUrl,
                    smallUrl = profilePictureSmallUrl,
                    mediumUrl = profilePictureMediumUrl)
        }
        return Profile(username = username, bio = bio, picture = profilePicture, isMe = isMe)
    }
}
