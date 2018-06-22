package ru.speechpro.stcspeechkit.domain.service

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
    private val recognizerApi: RecognizeApi
) {

    suspend fun closeSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        recognizerApi.closeSession(sessionId).await()
    }

    suspend fun checkSession(sessionId: String): Response<Void> = withContext(CommonPool) {
        recognizerApi.checkSession(sessionId).await()
    }

    suspend fun startSession(username: String, password: String, domainId: Int): Response<SessionIdResponse> = withContext(CommonPool) {
        recognizerApi.startSession(StartSessionRequest(username, password, domainId)).await()
    }

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