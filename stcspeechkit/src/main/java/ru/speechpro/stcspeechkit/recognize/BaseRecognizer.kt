package ru.speechpro.stcspeechkit.recognize

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.models.PackageResponse
import ru.speechpro.stcspeechkit.domain.service.RecognizerService
import ru.speechpro.stcspeechkit.util.Logger

/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
abstract class BaseRecognizer {

    val api = RecognizerService(STCSpeechKit.recognizeService)
    val job = Job()

    var session: String? = null

    companion object {
        private val TAG = BaseRecognizer::class.java.simpleName
    }

    suspend fun startSession(): String? {
        var sessionId: String?
        val response = api.startSession(STCSpeechKit.username, STCSpeechKit.password, STCSpeechKit.domainId)
        when {
            response.isSuccessful -> sessionId = response.body()?.sessionId
            else -> {
                val error = response.errorBody()?.string()
                val mapper = ObjectMapper()
                val errorResponse = mapper.readValue(error, ErrorResponse::class.java)

                throw Throwable("""Reason: ${errorResponse.reason}, message: ${errorResponse.message}""")
            }

        }
        return sessionId
    }

    suspend fun checkSession(sessionId: String): Boolean {
        var isOpenSession = false
        val response = api.checkSession(sessionId)
        when {
            response.code() == 200 -> isOpenSession = true
        }
        return isOpenSession
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