package ru.speechpro.stcspeechkit.recognize

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.common.AUDIO_ENCODING
import ru.speechpro.stcspeechkit.common.AUDIO_SOURCE
import ru.speechpro.stcspeechkit.common.CHANNELS
import ru.speechpro.stcspeechkit.common.PACKAGE_ADVANCED
import ru.speechpro.stcspeechkit.domain.models.Audio
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.models.RecognizeRequest
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.recognize.listeners.RecognizerListener
import ru.speechpro.stcspeechkit.util.AudioConverter
import ru.speechpro.stcspeechkit.util.Logger

/**
 * RestApiRecognizer class contains methods for recognize API
 *
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RestApiRecognizer private constructor(
        private var listener: RecognizerListener?,
        private val audioSource: Int,
        private val audioEncoding: Int,
        private val sampleRate: Int,
        private val channels: Int,
        private val packageId: String

) : BaseRecognizer(), IAudioRecorder, AudioListener {

    private val audioRecorder: AudioRecorder = AudioRecorder()

    companion object {
        private val TAG = RestApiRecognizer::class.java.simpleName
    }

    class Builder(recognizeListener: RecognizerListener) {
        private val recognizerListener = recognizeListener
        private var audioSource: Int = AUDIO_SOURCE
        private var audioEncoding: Int = AUDIO_ENCODING
        private var sampleRate: Int = 8000
        private var channels: Int = CHANNELS
        private var packageId: String = PACKAGE_ADVANCED

        fun audioSource(audioSource: Int) = apply { this.audioSource = audioSource }
        fun audioEncoding(audioEncoding: Int) = apply { this.audioEncoding = audioEncoding }
        fun sampleRate(sampleRate: Int) = apply { this.sampleRate = sampleRate }
        fun channels(channels: Int) = apply { this.channels = channels }
        fun packageId(packageId: String) = apply { this.packageId = packageId }

        fun build() = RestApiRecognizer(
                recognizerListener,
                audioSource,
                audioEncoding,
                sampleRate,
                channels,
                packageId
        )
    }

    private suspend fun recognize(sessionId: String, voice: ByteArray): String? {
        var recognizedText: String
        val response = api.sendVoiceToRecognize(sessionId, RecognizeRequest(Audio(voice, "audio/wav"), packageId))
        when {
            response.isSuccessful -> recognizedText = response.body()?.text!!
            else -> {
                val error = response.errorBody()?.string()
                val mapper = ObjectMapper()
                val errorResponse = mapper.readValue(error, ErrorResponse::class.java)

                throw Throwable("""Reason: ${errorResponse.reason}, message: ${errorResponse.message}""")
            }
        }

        return recognizedText
    }

    /**
     * It is necessary to call when the activity is destroyed
     */
    override fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null
        audioRecorder.cancel()

        super.destroy()
    }

    private fun launchCoroutine(bytesArray: ByteArray, hasWav: Boolean) {
        launch(job) {
            try {
                when {
                    session == null || !checkSession((session!!)) -> session = startSession()
                }
                loadPackage(session!!, packageId)
                val result = when {
                    hasWav -> recognize(session!!, bytesArray)
                    else -> recognize(session!!, AudioConverter.rawToWave(STCSpeechKit.applicationContext, bytesArray, sampleRate))
                }
                result?.let {
                    launch(UI) {
                        listener?.onRecognizerTextResult(it)
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

    /**
     * ByteArray Recognition
     *
     * @param ByteArray WAV data
     */
    fun recognize(bytesArray: ByteArray) {
        Logger.print(TAG, "recognize file " + bytesArray)

        if (bytesArray.size > 1048576 * 3) { // 1 Megabyte = 1048576 Bytes
            launch(UI) {
                listener?.onError("ByteArray is too big. Max. 3 MB per byte array.")
            }
            return
        }

        launchCoroutine(bytesArray, true)
    }

    /**
     * Start recording (need to request permission in realtime)
     */
    override fun startRecording() {
        Logger.print(TAG, "start recording...")
        audioRecorder.start(audioSource, sampleRate, channels, audioEncoding)
        audioRecorder.setAudioListener(this)
    }

    /**
     * Stop recording
     */
    override fun stopRecording() {
        Logger.print(TAG, "stop recording")
        audioRecorder.stop()
    }

    /**
     * Cancel recording
     */
    override fun cancelRecording() {
        Logger.print(TAG, "cancel recording")
        audioRecorder.cancel()
    }

    override fun onStart() {
        Logger.print(TAG, "onStart")
        launch(UI) {
            listener?.onRecordingStart()
        }
    }

    override fun onProcess(amplitude: Short) {
        Logger.print(TAG, "onProcess: $amplitude")
        launch(UI) {
            listener?.onPowerDbUpdate(amplitude)
        }
    }

    override fun onVoiceStream(stream: ByteArray?) {
        //not use in rest API
    }

    override fun onStop(voice: ByteArray?) {
        Logger.print(TAG, "onStop: $voice")
        launch(UI) {
            listener?.onRecordingStop()
        }
        voice?.let { launchCoroutine(it, false) }
    }

    override fun onCancel() {
        launch(UI) {
            listener?.onRecordingCancel()
        }
    }
}