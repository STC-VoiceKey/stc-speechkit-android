package ru.speechpro.stcspeechkit.data.network

import kotlinx.coroutines.experimental.Deferred
import retrofit2.Response
import retrofit2.http.*
import ru.speechpro.stcspeechkit.domain.models.DiarizationRequest
import ru.speechpro.stcspeechkit.domain.models.DiarizationResponse
import ru.speechpro.stcspeechkit.domain.models.SessionIdResponse
import ru.speechpro.stcspeechkit.domain.models.StartSessionRequest

/**
 * @author Alexander Grigal
 */
interface DiarizationApi {

    // session
    @DELETE("vkdiarization/rest/session")
    fun closeSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @GET("vkdiarization/rest/session")
    fun checkSession(@Header("X-Session-Id") sessionId: String): Deferred<Response<Void>>

    @POST("vkdiarization/rest/session")
    fun startSession(@Body session: StartSessionRequest): Deferred<Response<SessionIdResponse>>

    // diarization
    @POST("vkdiarization/rest/v1/diarization")
    fun getSpeechDiarization(
        @Header("X-Session-Id") sessionId: String,
        @Body request: DiarizationRequest
    ): Deferred<Response<DiarizationResponse>>
}