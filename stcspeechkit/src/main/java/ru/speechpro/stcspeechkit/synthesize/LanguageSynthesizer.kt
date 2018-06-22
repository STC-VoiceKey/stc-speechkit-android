package ru.speechpro.stcspeechkit.synthesize

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.synthesize.listeners.LanguageListener
import ru.speechpro.stcspeechkit.util.Logger

/**
 * LanguageSynthesizer class contains methods for synthesize API
 *
 * @author Alexander Grigal
 */
class LanguageSynthesizer constructor(
        private var listener: LanguageListener?

)   : BaseSynthesizer() {

    companion object {
        private val TAG = LanguageSynthesizer::class.java.simpleName
    }

    fun getAvailableLanguages() {
        Logger.print(TAG, "getAvailablePackages")

        launch(job) {
            try {
                when {
                    session == null || !checkSession(session!!) -> session = startSession()
                }
                when {
                    session != null -> {
                        Logger.print(TAG, session!!)

                        val result = availableLanguages(session!!)
                        result?.let {
                            Logger.print(TAG, result.toString())
                            launch(UI) {
                                listener?.onAvailableLanguages(result)
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                launch(UI) {
                    listener?.onError(throwable.message!!)
                }
            }
        }

    }

    fun getLanguageInfo(language: Language) {
        Logger.print(TAG, "getLanguageInfo")

        launch(job) {
            try {
                when {
                    session == null || !checkSession(session!!) -> session = startSession()
                }
                when {
                    session != null -> {
                        Logger.print(TAG, session!!)

                        val result = languageInfo(session!!, language.name)
                        result?.let {
                            Logger.print(TAG, result.toString())
                            launch(UI) {
                                listener?.onLanguageInfo(result)
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                launch(UI) {
                    listener?.onError(throwable.message!!)
                }
            }
        }

    }

    /**
     * It is necessary to call when the activity is destroyed
     */
    override fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null

        super.destroy()
    }

}