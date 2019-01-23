package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.AntiSpoofingApi
import ru.speechpro.stcspeechkit.domain.models.AntiSpoofingResponse
import ru.speechpro.stcspeechkit.domain.models.DataRequest


/**
 * @author Alexander Grigal
 */
class AntiSpoofingService constructor(
        private val antiSpoofingApi: AntiSpoofingApi,
        sessionClient: SessionClientFactory.SessionClient
) : BaseService(sessionClient) {

    suspend fun sendVoiceToAntiSpoofing(sessionId: String, request: DataRequest): Response<AntiSpoofingResponse> = withContext(CommonPool) {
        antiSpoofingApi.getAntiSpoofingResult(sessionId, request).await()
    }

}