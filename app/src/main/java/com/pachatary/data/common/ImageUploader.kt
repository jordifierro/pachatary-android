package com.pachatary.data.common

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.BuildConfig
import com.pachatary.data.auth.AuthHttpInterceptor
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import net.gotev.uploadservice.*
import java.net.UnknownHostException

class ImageUploader(val context: Context, val authHttpInterceptor: AuthHttpInterceptor,
                    val clientVersionHttpInterceptor: ClientVersionHttpInterceptor) {

    fun upload(imageUriString: String, relativePath: String): Flowable<Result<JsonObject>> =
        Flowable.create<Result<JsonObject>>({ emitter ->
            try {
                MultipartUploadRequest(context,
                        BuildConfig.API_URL + relativePath)
                        .addFileToUpload(Uri.parse(imageUriString).path, "picture")
                        .setNotificationConfig(UploadNotificationConfig())
                        .setMaxRetries(3)
                        .addHeader(authHttpInterceptor.key(),
                                   authHttpInterceptor.value())
                        .addHeader(clientVersionHttpInterceptor.key(),
                                   clientVersionHttpInterceptor.value())
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
                                emitter.onNext(ResultSuccess(JsonParser().parse(
                                        serverResponse!!.bodyAsString).asJsonObject))
                                emitter.onComplete()
                            }
                        })
                        .startUpload()
            } catch (exc: Exception) {
                emitter.onError(exc)
            }
        }, BackpressureStrategy.LATEST)
                .startWith(ResultInProgress())
}