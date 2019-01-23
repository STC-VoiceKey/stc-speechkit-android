package ru.speechpro.stcspeechkit.synthesize.listeners

import ru.speechpro.stcspeechkit.domain.models.LangResponse
import ru.speechpro.stcspeechkit.domain.models.LangVoicesResponse
import ru.speechpro.stcspeechkit.interfaces.BaseListener

/**
 * Implemented this in order for LanguageSynthesizer to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface LanguageListener : BaseListener {

    /**
     * Called when languages are received
     *
     * @param languages list
     */
    fun onAvailableLanguages(languages: List<LangResponse>)

    /**
     * Called when language info are received
     *
     * @param list voices info
     */
    fun onLanguageInfo(list: List<LangVoicesResponse>)

}