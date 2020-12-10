package ru.speechpro.stcspeechkit.data.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import ru.speechpro.stcspeechkit.domain.models.AntiSpoofingResponse
import ru.speechpro.stcspeechkit.domain.models.DataRequest


/**
 * @author Alexander Grigal
 */
interface AntiSpoofingApi {
    // anti spoofing
    @POST("vkantispoofing/rest/v1/inspect")
    fun getAntiSpoofingResult(
            @Header("X-Session-Id") sessionId: String,
            @Body request: DataRequest
    ): Deferred<Response<AntiSpoofingResponse>>
}