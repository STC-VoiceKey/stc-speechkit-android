package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun sendVoiceToDiarization(sessionId: String, request: DiarizationRequest): Response<DiarizationResponse> = withContext(Dispatchers.IO) {
        diarizationApi.getSpeechDiarization(sessionId, request).await()
    }
}