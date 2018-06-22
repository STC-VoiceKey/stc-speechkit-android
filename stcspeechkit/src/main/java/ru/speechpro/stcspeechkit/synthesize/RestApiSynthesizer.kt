package ru.speechpro.stcspeechkit.synthesize

import android.media.MediaPlayer
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.common.LANGUAGE_IS_NOT_SUPPORTED
import ru.speechpro.stcspeechkit.common.SPEAKER_IS_NOT_SUPPORTED
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.models.SynthesizeRequest
import ru.speechpro.stcspeechkit.domain.models.Text
import ru.speechpro.stcspeechkit.synthesize.listeners.SynthesizerListener
import ru.speechpro.stcspeechkit.util.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * RestApiSynthesizer class contains methods for synthesize API
 *
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RestApiSynthesizer private constructor(
        private var listener: SynthesizerListener?,
        private val language: Language,
        private val speaker: String

) : BaseSynthesizer() {

    companion object {
        private val TAG = RestApiSynthesizer::class.java.simpleName
    }

    class Builder(synthesizerListener: SynthesizerListener) {
        private val listener = synthesizerListener
        private var language: Language = Language.Russian
        private var speaker: String = "Alexander"

        fun language(language: Language) = apply { this.language = language }
        fun speaker(speaker: String) = apply { this.speaker = speaker }

        fun build() = RestApiSynthesizer(
                listener,
                language,
                speaker
        )
    }

    private suspend fun synthesize(sessionId: String, speaker: String, text: String): ByteArray? {
        var result: ByteArray?
        val response = api.sendTextToSynthesize(sessionId, SynthesizeRequest(speaker, Text("text/plain", text), "audio/wav"))

        when {
            response.isSuccessful -> result = response.body()?.data!!
            else -> {
                val error = response.errorBody()?.string()
                val mapper = ObjectMapper()
                val errorResponse = mapper.readValue(error, ErrorResponse::class.java)

                throw Throwable("""Reason: ${errorResponse.reason}, message: ${errorResponse.message}""")
            }
        }

        return result
    }

    /**
     * It is necessary to call when the activity is destroyed
     */
    override fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null

        super.destroy()
    }

    /**
     * Synthesize Voice
     *
     * @param text synthesized text
     */
    fun synthesize(text: String) {
        Logger.print(TAG, "synthesize: $text")

        launch(job) {
            try {
                when {
                    session == null || !checkSession((session!!)) -> session = startSession()
                }
                Logger.print(TAG, session!!)

                if (!isSupportedLanguage(session!!, language)) {
                    Logger.print(TAG, LANGUAGE_IS_NOT_SUPPORTED)
                    launch(UI) {
                        listener?.onError(LANGUAGE_IS_NOT_SUPPORTED)
                    }
                    return@launch
                }

                if (!containsSpeakerForLanguage(session!!, language, speaker)) {
                    Logger.print(TAG, SPEAKER_IS_NOT_SUPPORTED)
                    launch(UI) {
                        listener?.onError(SPEAKER_IS_NOT_SUPPORTED)
                    }
                    return@launch
                }

                val result = synthesize(session!!, speaker, text)
                when {
                    result != null -> {
                        playWav(result)
                        launch(UI) {
                            listener?.onSynthesizerResult(result)
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.print(TAG, throwable.message!!)
                launch(UI) {
                    listener?.onError(throwable.message!!)
                }
            }
        }
    }

    private fun playWav(wavByteArray: ByteArray) {
        val pathName = STCSpeechKit.applicationContext.getCacheDir().toString() + "/file.wav"
        val path = File(pathName)

        val fos = FileOutputStream(path)
        fos.write(wavByteArray)
        fos.close()

        val mediaPlayer = MediaPlayer()

        FileInputStream(path)
        mediaPlayer.setDataSource(pathName)

        mediaPlayer.prepare()
        mediaPlayer.start()
    }
}