package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.SynthesizeApi
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
class SynthesizerService constructor(
    private val synthesizerApi: SynthesizeApi,
    sessionClient: SessionClientFactory.SessionClient
) : BaseService(sessionClient) {

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