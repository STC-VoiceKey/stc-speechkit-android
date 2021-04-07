package ru.speechpro.stcspeechkit.recognize

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.domain.models.PackageResponse
import ru.speechpro.stcspeechkit.domain.service.RecognizerV2Service
import ru.speechpro.stcspeechkit.util.Logger

/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
abstract class BaseRecognizer {

    val api = RecognizerV2Service(STCSpeechKit.recognizeV2Service, STCSpeechKit.sessionClient)
    var job = Job()

    var session: String? = null

    companion object {
        private val TAG = BaseRecognizer::class.java.simpleName
    }

    suspend fun startSession(): String? {
        return api.startSession(STCSpeechKit.username, STCSpeechKit.password, STCSpeechKit.domainId)
    }

    suspend fun checkSession(sessionId: String): Boolean {
        return api.checkSession(sessionId)
    }

    suspend fun closeSession(sessionId: String) {
        api.closeSession(sessionId)
    }

    suspend fun availablePackages(sessionId: String): List<PackageResponse>? {
        val response = api.getAllModels(sessionId)
        when {
            response.isSuccessful -> return response.body()?.map { e -> PackageResponse(true, e.sampleRate, e.name, e.language, e.modelId) }
        }

        return null
    }

    suspend fun loadPackage(sessionId: String, packageId: String) {
        //not implemented in v2
    }

    suspend fun unloadPackage(sessionId: String, packageId: String) {
        //not implemented in v2
    }

    open fun destroy() {
        Logger.print(TAG, "destroy")

        session?.let {
            GlobalScope.launch(Dispatchers.Main) {
                // в случае эксепшена - краш приложения
                try {
                    closeSession(it)
                } catch (ex: Exception) {
                    Logger.print(TAG, "RestApiAntiSpoofing $ex")
                }
                session = null
            }
        }
        job.cancel()
    }

}
