package ru.speechpro.stcspeechkit.domain.service

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.DiarizationApi
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
class DiarizationService constructor(
    private val diarizationApi: DiarizationApi
) {

    suspend fun closeSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        diarizationApi.closeSession(sessionId).await()
    }

    suspend fun checkSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        diarizationApi.checkSession(sessionId).await()
    }

    suspend fun startSession(username: String, password: String, domainId: Int): Response<SessionIdResponse> = withContext(CommonPool) {
        diarizationApi.startSession(StartSessionRequest(username, password, domainId)).await()
    }

    suspend fun sendVoiceToDiarization(sessionId: String, request: DiarizationRequest): Response<DiarizationResponse> = withContext(CommonPool) {
        diarizationApi.getSpeechDiarization(sessionId, request).await()
    }
}