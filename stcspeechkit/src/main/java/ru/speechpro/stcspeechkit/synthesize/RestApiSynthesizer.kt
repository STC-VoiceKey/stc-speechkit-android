package ru.speechpro.stcspeechkit.synthesize

import android.media.MediaPlayer
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.common.LANGUAGE_IS_NOT_SUPPORTED
import ru.speechpro.stcspeechkit.common.SPEAKER_IS_NOT_SUPPORTED
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.models.SynthesizeRequest
import ru.speechpro.stcspeechkit.domain.models.Text
import ru.speechpro.stcspeechkit.synthesize.listeners.SynthesizerListener
import ru.speechpro.stcspeechkit.util.Logger
import java.io.File
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
        stopMediaPlayer()

        super.destroy()
    }

    /**
     * Synthesize Voice
     *
     * @param text synthesized text
     */
    fun synthesize(text: String) {
        Logger.print(TAG, "synthesize: $text")

        // Не уверен в актуальности данного исправления, т.к. в данный момент для каждой итераций
        // распознавания мы создаем новый экзмепляр RestApiSynthesizer
        // (отдельно пересоздавать job в таком сценарии нет необходимости)
        if (job.isCancelled) {
            job = Job()
        }

        GlobalScope.launch(job) {
            try {
                when {
                    session == null || !checkSession((session!!)) -> session = startSession()
                }
                Logger.print(TAG, session!!)

                if (!isSupportedLanguage(session!!, language)) {
                    Logger.print(TAG, LANGUAGE_IS_NOT_SUPPORTED)
                    launch(Dispatchers.Main) {
                        listener?.onError(LANGUAGE_IS_NOT_SUPPORTED)
                    }
                    return@launch
                }

                if (!containsSpeakerForLanguage(session!!, language, speaker)) {
                    Logger.print(TAG, SPEAKER_IS_NOT_SUPPORTED)
                    launch(Dispatchers.Main) {
                        listener?.onError(SPEAKER_IS_NOT_SUPPORTED)
                    }
                    return@launch
                }

                val result = synthesize(session!!, speaker, text)
                when {
                    result != null -> {
                        playWav(result)
                        launch(Dispatchers.Main) {
                            listener?.onSynthesizerResult(result)
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.print(TAG, throwable.message!!)
                launch(Dispatchers.Main) {
                    listener?.onError(throwable.message!!)
                }
            }
        }
    }

    private fun playWav(wavByteArray: ByteArray) {
        val pathName = STCSpeechKit.applicationContext.getCacheDir().toString() + "/file.wav"
        val path = File(pathName)

        FileOutputStream(path).use { it.write(wavByteArray) }

        stopMediaPlayer()
        mediaPlayer = startNewMediaPlayer(pathName)
    }

    /** Костыль, т.к. в апи отсутствует способ остановить голос
     * 1) нам нужен в апи метод stopSynthesizing(), который остановит и запрос на сервер и воспроизведение голоса
     * 2) текущая апи создавала новый MediaPlayer на каждое проигрывание и никогда не освобождала его
     * это заканчивалось исчерпание системных ресурсов и невозможностью воспроизводить звук
     *
     * Либо пусть библиотека не берет на себя ответственность за запись и проигрывание файла, а только возращают нам
     * его byteArray в листенере
     * */
    private var mediaPlayer: MediaPlayer? = null
    private fun startNewMediaPlayer(pathName: String) = MediaPlayer().run {
        try {
            setOnCompletionListener { listener?.onSynthesizerComplete() }
            setDataSource(pathName)
            prepare()
            start()
            this
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}\n$e")
            releaseMediaPlayer(this)
            null
        }
    }
    fun stopMediaPlayer() {
        mediaPlayer?.let { releaseMediaPlayer(it) }
        mediaPlayer = null
    }
    private fun releaseMediaPlayer(mp: MediaPlayer) = mp.apply {
        try {
            if (isPlaying) { stop() }
            // важно вызывать и reset() и release()
            // опыты показали:
            // только при этих обоих вызовах при многочисленных проигрываниях
            // MediaPlayer не исчерпывает системные ресурсы
            reset()
            release()
        } catch(e: Exception) {
            Log.e(TAG, "${e.message}\n$e")
        }
    }

}