package ru.speechpro.stcspeechkit.diarization

import ru.speechpro.stcspeechkit.domain.models.SpeakersItem
import ru.speechpro.stcspeechkit.interfaces.AudioRecorderListener
import ru.speechpro.stcspeechkit.interfaces.BaseListener

/**
 * Implemented this in order for RestApiDiarization to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface DiarizationListener : AudioRecorderListener, BaseListener {

    /**
     * Called when the text is diarization.
     *
     * @param result text
     */
    fun onDiarizationResult(result: List<SpeakersItem>)
}