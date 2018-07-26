package com.pachatary.data.profile

import com.google.gson.JsonObject
import com.pachatary.data.common.*
import com.pachatary.data.picture.LittlePicture
import io.reactivex.Flowable
import io.reactivex.Scheduler
import retrofit2.Retrofit
import javax.inject.Named

class ProfileApiRepository(retrofit: Retrofit, @Named("io") val ioScheduler: Scheduler,
                           val imageUploader: ImageUploader) {

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
            imageUploader.upload(imageUriString, "/profiles/me/picture")
                .subscribeOn(ioScheduler)
                .map {
                    when {
                        it.isInProgress() -> ResultInProgress()
                        it.isError() -> ResultError(it.error!!)
                        else -> ResultSuccess(parseProfileJson(it.data!!))
                    }
                }

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
