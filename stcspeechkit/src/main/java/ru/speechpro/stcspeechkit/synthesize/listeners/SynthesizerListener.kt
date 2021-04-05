package ru.speechpro.stcspeechkit.synthesize.listeners

import ru.speechpro.stcspeechkit.interfaces.BaseListener

/**
 * Implemented this in order for RestApiSynthesizer to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface SynthesizerListener : BaseListener {

    /**
     * Called when a synthesis result.(Only for REST API)
     *
     * @param byteArray voice WAV
     */
    fun onSynthesizerResult(byteArray: ByteArray)

    /** Вызывается после завершения озвучки текста */
    fun onSynthesizerComplete()
}
