package ru.speechpro.stcspeechkit.domain.service

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.SynthesizeApi
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
class SynthesizerService constructor(
    private val synthesizerApi: SynthesizeApi
) {

    suspend fun closeSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        synthesizerApi.closeSession(sessionId).await()
    }

    suspend fun checkSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        synthesizerApi.checkSession(sessionId).await()
    }

    suspend fun startSession(username: String, password: String, domainId: Int): Response<SessionIdResponse> = withContext(CommonPool) {
        synthesizerApi.startSession(StartSessionRequest(username, password, domainId)).await()
    }

    suspend fun getAvailableLanguages(sessionId: String): Response<List<LangResponse>> = withContext(CommonPool) {
        synthesizerApi.getAvailableLanguages(sessionId).await()
    }

    suspend fun getLanguageInfo(sessionId: String, language: String): Response<List<LangVoicesResponse>> = withContext(CommonPool) {
        synthesizerApi.getAvailableLanguages(sessionId, language).await()
    }

    suspend fun sendTextToSynthesize(sessionId: String, request: SynthesizeRequest): Response<DataResponse> = withContext(CommonPool) {
        synthesizerApi.getSynthesizeSpeech(sessionId, request).await()
    }

    suspend fun openStream(sessionId: String, request: StreamSynthesizeRequest): Response<StreamResponse> = withContext(CommonPool) {
        synthesizerApi.startSynthesizeStream(sessionId, request).await()
    }

    suspend fun closeStream(sessionId: String, transactionId: String): Response<CloseStreamResponse> = withContext(CommonPool) {
        synthesizerApi.closeSynthesizeStream(sessionId, transactionId).await()
    }
}