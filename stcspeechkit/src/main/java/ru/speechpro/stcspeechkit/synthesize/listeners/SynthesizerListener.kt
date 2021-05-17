package ru.speechpro.stcspeechkit.synthesize.listeners

import ru.speechpro.stcspeechkit.interfaces.BaseListener

/**
 * Implemented this in order for RestApiSynthesizer to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface SynthesizerListener : BaseListener {

    /**
     * Called when a synthesis result.
     *
     * (Final result for REST API, or partial when WebSocket using)
     *
     * @param byteArray voice WAV
     */
    fun onSynthesizerResult(byteArray: ByteArray)

    /** Вызывается после завершения озвучки текста */
    fun onSynthesizerComplete()
}
