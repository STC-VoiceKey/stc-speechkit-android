package ru.speechpro.stcspeechkit.data.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
interface SynthesizeApi {

    // session
    @DELETE("vktts/rest/session")
    fun closeSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @GET("vktts/rest/session")
    fun checkSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @POST("vktts/rest/session")
    fun startSession(@Body session: StartSessionRequest): Deferred<Response<SessionIdResponse>>

    // languages
    @GET("vktts/rest/v1/languages")
    fun getAvailableLanguages(@Header("X-Session-Id") sessionId: String): Deferred<Response<List<LangResponse>>>

    @GET("vktts/rest/v1/languages/{language}/voices")
    fun getAvailableLanguages(
        @Header("X-Session-Id") sessionId: String,
        @Path("language") language: String
    ): Deferred<Response<List<LangVoicesResponse>>>

    // synthesize
    @POST("vktts/rest/v1/synthesize")
    fun getSynthesizeSpeech(
        @Header("X-Session-Id") sessionId: String,
        @Body request: SynthesizeRequest
    ): Deferred<Response<DataResponse>>

    // stream
    @DELETE("vktts/rest/v1/synthesize/stream")
    fun closeSynthesizeStream(
        @Header("X-Session-Id") sessionId: String,
        @Header("X-Transaction-Id") transactionId: String
    ): Deferred<Response<CloseStreamResponse>>

    @POST("vktts/rest/v1/synthesize/stream")
    fun startSynthesizeStream(
        @Header("X-Session-Id") sessionId: String,
        @Body request: StreamSynthesizeRequest
    ): Deferred<Response<StreamResponse>>
}