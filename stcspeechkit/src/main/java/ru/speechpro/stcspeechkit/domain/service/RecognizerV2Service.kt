package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.RecognizeV2Api
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RecognizerV2Service constructor(
        private val recognizerApi: RecognizeV2Api,
        sessionClient: SessionClientFactory.SessionClient
) : BaseService(sessionClient) {

    suspend fun getAllModels(sessionId: String): Response<List<ModelResponse>> = withContext(Dispatchers.IO) {
        recognizerApi.getAllModels(sessionId).await()
    }

    suspend fun sendVoiceToRecognize(sessionId: String, request: RecognizeV2Request): Response<RecognizeV2Response> = withContext(Dispatchers.IO) {
        recognizerApi.recognizeText(sessionId, request).await()
    }

    suspend fun openStream(sessionId: String, request: StartTransactionRequest): Response<StreamResponse> = withContext(Dispatchers.IO) {
        recognizerApi.startWebsocketTransaction(sessionId, request).await()
    }

    suspend fun closeStream(sessionId: String, transactionId: String): Response<RecognizeV2Response> = withContext(Dispatchers.IO) {
        recognizerApi.closeTransaction(sessionId, transactionId).await()
    }
}