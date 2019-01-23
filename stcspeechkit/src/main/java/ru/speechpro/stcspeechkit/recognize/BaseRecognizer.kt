package ru.speechpro.stcspeechkit.recognize

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.domain.models.PackageResponse
import ru.speechpro.stcspeechkit.domain.service.RecognizerService
import ru.speechpro.stcspeechkit.util.Logger

/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
abstract class BaseRecognizer {

    val api = RecognizerService(STCSpeechKit.recognizeService, STCSpeechKit.sessionClient)
    val job = Job()

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
        val response = api.getAllPackages(sessionId)
        when {
            response.isSuccessful -> return response.body()!!
        }

        return null
    }

    suspend fun loadPackage(sessionId: String, packageId: String) {
        api.loadPackage(sessionId, packageId)
    }

    suspend fun unloadPackage(sessionId: String, packageId: String) {
        api.unloadPackage(sessionId, packageId)
    }

    open fun destroy() {
        Logger.print(TAG, "destroy")

        session?.let {
            launch(UI) {
                closeSession(it)
            }
        }
        job.cancel()
    }

}