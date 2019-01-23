package ru.speechpro.stcspeechkit.domain.service

import com.speechpro.android.session.session_library.SessionClientFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext


/**
 * @author Alexander Grigal
 */
abstract class BaseService constructor(
        private val sessionClient: SessionClientFactory.SessionClient
) {

    suspend fun closeSession(sessionId: String): Boolean = withContext(CommonPool) {
        sessionClient.closeSession(sessionId)
    }

    suspend fun checkSession(sessionId: String): Boolean = withContext(CommonPool) {
        sessionClient.checkSession(sessionId)
    }

    suspend fun startSession(username: String, password: String, domainId: Int): String = withContext(CommonPool) {
        sessionClient.openSession(username, password, domainId)
    }

}