package ru.speechpro.stcspeechkit.anti_spoofing

import ru.speechpro.stcspeechkit.domain.models.AntiSpoofingResponse
import ru.speechpro.stcspeechkit.interfaces.AudioRecorderListener
import ru.speechpro.stcspeechkit.interfaces.BaseListener


/**
 * @author Alexander Grigal
 */
interface AntiSpoofingListener : AudioRecorderListener, BaseListener {

    /**
     *
     * @param result AntiSpoofingResponse object
     */
    fun onAntiSpoofingResult(result: AntiSpoofingResponse)
}