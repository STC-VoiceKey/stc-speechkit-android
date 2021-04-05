package ru.speechpro.stcspeechkit.synthesize

import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.domain.models.LangResponse
import ru.speechpro.stcspeechkit.domain.models.LangVoicesResponse
import ru.speechpro.stcspeechkit.domain.service.SynthesizerService
import ru.speechpro.stcspeechkit.util.Logger


/**
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
abstract class BaseSynthesizer {

    val api = SynthesizerService(STCSpeechKit.synthesizeService, STCSpeechKit.sessionClient)
    var job = Job()

    var session: String? = null

    companion object {
        private val TAG = BaseSynthesizer::class.java.simpleName
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

    suspend fun isSupportedLanguage(sessionId: String, language: Language): Boolean {
        val response = api.getAvailableLanguages(sessionId)

        if (response.isSuccessful) {
            val languages = response.body()
            if (languages == null || languages.isEmpty()) {
                return false
            }

            for (lang in languages) {
                if (lang.name.equals(language.name)) {
                    return true
                }
            }
        } else {
            throw Throwable(response.message())
        }

        return false
    }

    suspend fun getFirstSpeaker(sessionId: String, language: Language): String? {
        val response = api.getLanguageInfo(sessionId, language.name)

        if (response.isSuccessful) {
            val voices = response.body()
            if (voices != null && !voices.isEmpty())
                return voices.get(0).name
        }

        return null
    }

    suspend fun containsSpeakerForLanguage(sessionId: String, language: Language, speaker: String): Boolean {
        val response = api.getLanguageInfo(sessionId, language.name)

        if (response.isSuccessful) {
            val voices = response.body()
            if (voices != null && !voices.isEmpty()) {
                voices.forEach { voice ->
                    when {
                        voice.name.equals(speaker) -> return true
                    }
                }
            }

        }

        return false
    }

    suspend fun availableLanguages(sessionId: String): List<LangResponse>? {
        val response = api.getAvailableLanguages(sessionId)

        when {
            response.isSuccessful -> return response.body()
            else -> return null
        }
    }

    suspend fun languageInfo(sessionId: String, language: String): List<LangVoicesResponse>? {
        val response = api.getLanguageInfo(sessionId, language)

        when {
            response.isSuccessful -> return response.body()
            else -> return null
        }
    }

    open fun destroy() {
        Logger.print(TAG, "destroy")

        session?.let {
            GlobalScope.launch(Dispatchers.Main) {
                closeSession(it)
                session = null
            }
        }
        job.cancel()
    }

}