package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * @author Alexander Grigal
 */
abstract class BaseService constructor(
        private val sessionClient: SessionClientFactory.SessionClient
) {

    suspend fun closeSession(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        sessionClient.closeSession(sessionId)
    }

    suspend fun checkSession(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        sessionClient.checkSession(sessionId)
    }

    suspend fun startSession(username: String, password: String, domainId: Int): String = withContext(Dispatchers.IO) {
        sessionClient.openSession(username, password, domainId)
    }

}