package ru.speechpro.stcspeechkit.data.network

import kotlinx.coroutines.experimental.Deferred
import retrofit2.Response
import retrofit2.http.*
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
interface RecognizeApi {

    // session
    @DELETE("vkasr/rest/session")
    fun closeSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @GET("vkasr/rest/session")
    fun checkSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @POST("vkasr/rest/session")
    fun startSession(@Body session: StartSessionRequest): Deferred<Response<SessionIdResponse>>

    // packages
    @GET ("vkasr/rest/v1/packages/available")
    fun getAllPackages(@Header("X-Session-Id") sessionId: String): Deferred<Response<List<PackageResponse>>>

    @GET ("vkasr/rest/v1/packages/{packageId}/load")
    fun loadPackage(
        @Header("X-Session-Id") sessionId: String,
        @Path("packageId") packageName: String
    ): Deferred<Response<Void>>

    @GET ("vkasr/rest/v1/packages/{packageId}/unload")
    fun unloadPackage(
        @Header("X-Session-Id") sessionId: String,
        @Path("packageId") packageName: String
    ): Deferred<Response<Void>>

    // recognize
    @POST ("vkasr/rest/v1/recognize")
    fun getSpeechRecognition(
        @Header("X-Session-Id") sessionId: String,
        @Body request: RecognizeRequest
    ): Deferred<Response<RecognizeResponse>>

    @POST ("vkasr/rest/v1/recognize/words")
    fun getWordList(
        @Header("X-Session-Id") sessionId: String,
        @Body request: RecognizeRequest
    ): Deferred<Response<List<RecognizeWordResponse>>>

    @DELETE ("vkasr/rest/v1/recognize/stream")
    fun closeRecognitionStream(
        @Header("X-Session-Id") sessionId: String,
        @Header("X-Transaction-Id") transactionId: String
    ): Deferred<Response<RecognizeResponse>>

    @POST ("vkasr/rest/v1/recognize/stream")
    fun startRecognitionStream(
        @Header("X-Session-Id") sessionId: String,
        @Body request: StreamRecognizeRequest
    ): Deferred<Response<StreamResponse>>
}