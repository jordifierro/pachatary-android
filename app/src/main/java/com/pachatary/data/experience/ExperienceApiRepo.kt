package com.pachatary.data.experience

import com.google.gson.JsonObject
import com.pachatary.data.common.*
import com.pachatary.data.picture.BigPicture
import com.pachatary.data.picture.LittlePicture
import com.pachatary.data.profile.Profile
import io.reactivex.Flowable
import io.reactivex.Scheduler
import retrofit2.Retrofit
import javax.inject.Named

class ExperienceApiRepo(retrofit: Retrofit, @Named("io") val ioScheduler: Scheduler,
                        val imageUploader: ImageUploader) : ExperienceApiRepository {

    private val experienceApi: ExperienceApi = retrofit.create(ExperienceApi::class.java)

    override fun exploreExperiencesFlowable(word: String?, latitude: Double?, longitude: Double?)
            : Flowable<Result<List<Experience>>> =
            experienceApi.exploreExperiences(word, latitude, longitude)
                .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                .subscribeOn(ioScheduler)

    override fun myExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.myExperiences()
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(ioScheduler)

    override fun savedExperiencesFlowable(): Flowable<Result<List<Experience>>> =
            experienceApi.savedExperiences()
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(ioScheduler)

    override fun personsExperienceFlowable(username: String): Flowable<Result<List<Experience>>> =
            experienceApi.personsExperiences(username)
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(ioScheduler)

    override fun paginateExperiences(url: String): Flowable<Result<List<Experience>>> =
            experienceApi.paginateExperiences(url)
                    .compose(NetworkParserFactory.getPaginatedListTransformer<Experience, ExperienceMapper>())
                    .subscribeOn(ioScheduler)

    override fun experienceFlowable(experienceId: String): Flowable<Result<Experience>> =
            experienceApi.getExperience(experienceId)
                    .subscribeOn(ioScheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())

    override fun createExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.createExperience(title = experience.title, description = experience.description)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)

    override fun editExperience(experience: Experience): Flowable<Result<Experience>> =
            experienceApi.editExperience(experience.id, experience.title, experience.description)
                    .compose(NetworkParserFactory.getTransformer())
                    .subscribeOn(ioScheduler)

    override fun saveExperience(save: Boolean, experienceId: String): Flowable<Result<Void>> {
        if (save) return experienceApi.saveExperience(experienceId)
                    .compose(NetworkParserFactory.getVoidTransformer())
                    .subscribeOn(ioScheduler)
        else return experienceApi.unsaveExperience(experienceId)
                    .compose(NetworkParserFactory.getVoidTransformer())
                    .subscribeOn(ioScheduler)
    }

    override fun getShareUrl(experienceId: String): Flowable<Result<String>> =
            experienceApi.getExperienceShareUrl(experienceId)
                    .subscribeOn(ioScheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())

    override fun translateShareId(experienceShareId: String): Flowable<Result<String>> =
            experienceApi.translateShareId(experienceShareId)
                    .subscribeOn(ioScheduler)
                    .compose(NetworkParserFactory.getTransformer())
                    .startWith(ResultInProgress())

    override fun uploadExperiencePicture(experienceId: String, imageUriString: String)
                                                                    : Flowable<Result<Experience>> =
            imageUploader.upload(imageUriString, "/experiences/$experienceId/picture")
                    .subscribeOn(ioScheduler)
                    .map {
                        when {
                            it.isInProgress() -> ResultInProgress()
                            it.isError() -> ResultError(it.error!!)
                            else -> ResultSuccess(parseExperienceJson(it.data!!))
                        }
                    }

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
