package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import retrofit2.Response
import ru.speechpro.stcspeechkit.data.network.RecognizeApi
import ru.speechpro.stcspeechkit.domain.models.*

/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RecognizerService constructor(
    private val recognizerApi: RecognizeApi,
    sessionClient: SessionClientFactory.SessionClient
) : BaseService(sessionClient) {

    suspend fun getAllPackages(sessionId: String): Response<List<PackageResponse>> = withContext(CommonPool) {
        recognizerApi.getAllPackages(sessionId).await()
    }

    suspend fun loadPackage(sessionId: String, packageId: String): Response<Void> = withContext(CommonPool) {
        recognizerApi.loadPackage(sessionId, packageId).await()
    }

    suspend fun unloadPackage(sessionId: String, packageId: String): Response<Void> = withContext(CommonPool) {
        recognizerApi.unloadPackage(sessionId, packageId).await()
    }

    suspend fun sendVoiceToRecognize(sessionId: String, request: RecognizeRequest): Response<RecognizeResponse> = withContext(CommonPool) {
        recognizerApi.getSpeechRecognition(sessionId, request).await()
    }

    suspend fun openStream(sessionId: String, request: StreamRecognizeRequest): Response<StreamResponse> = withContext(CommonPool) {
        recognizerApi.startRecognitionStream(sessionId, request).await()
    }

    suspend fun closeStream(sessionId: String, transactionId: String): Response<RecognizeResponse> = withContext(CommonPool) {
        recognizerApi.closeRecognitionStream(sessionId, transactionId).await()
    }
}