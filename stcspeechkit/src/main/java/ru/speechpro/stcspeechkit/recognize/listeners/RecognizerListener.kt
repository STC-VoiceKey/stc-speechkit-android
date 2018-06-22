package ru.speechpro.stcspeechkit.recognize.listeners

import ru.speechpro.stcspeechkit.interfaces.AudioRecorderListener
import ru.speechpro.stcspeechkit.interfaces.BaseListener

/**
 * Implemented this in order for RestApiRecognizer to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface RecognizerListener : AudioRecorderListener, BaseListener {

    /**
     * Called when the text is recognized.
     *
     * @param result text
     */
    fun onRecognizerTextMessage(result: String)

    /**
     * Called when the text is finally recognized.
     *
     * @param result text
     */
    fun onRecognizerTextResult(result: String)
}