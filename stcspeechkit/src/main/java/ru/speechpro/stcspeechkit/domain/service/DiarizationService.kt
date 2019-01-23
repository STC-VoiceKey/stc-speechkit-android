package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.DiarizationApi
import ru.speechpro.stcspeechkit.domain.models.DiarizationRequest
import ru.speechpro.stcspeechkit.domain.models.DiarizationResponse

/**
 * @author Alexander Grigal
 */
class DiarizationService constructor(
    private val diarizationApi: DiarizationApi,
    sessionClient: SessionClientFactory.SessionClient
) : BaseService(sessionClient) {

    suspend fun sendVoiceToDiarization(sessionId: String, request: DiarizationRequest): Response<DiarizationResponse> = withContext(CommonPool) {
        diarizationApi.getSpeechDiarization(sessionId, request).await()
    }
}