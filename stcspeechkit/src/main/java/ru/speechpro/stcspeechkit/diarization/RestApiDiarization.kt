package ru.speechpro.stcspeechkit.diarization

import android.support.annotation.RequiresPermission
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.STCSpeechKit.domainId
import ru.speechpro.stcspeechkit.STCSpeechKit.password
import ru.speechpro.stcspeechkit.STCSpeechKit.username
import ru.speechpro.stcspeechkit.common.AUDIO_ENCODING
import ru.speechpro.stcspeechkit.common.AUDIO_SOURCE
import ru.speechpro.stcspeechkit.common.CHANNELS
import ru.speechpro.stcspeechkit.domain.models.Audio
import ru.speechpro.stcspeechkit.domain.models.Data
import ru.speechpro.stcspeechkit.domain.models.DiarizationRequest
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.service.DiarizationService
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.util.AudioConverter
import ru.speechpro.stcspeechkit.util.Logger

/**
 * RestApiDiarization class contains methods for diarization API
 *
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RestApiDiarization private constructor(
        private var listener: DiarizationListener?,
        private val audioSource: Int,
        private val audioEncoding: Int,
        private val sampleRate: Int,
        private val channels: Int

) : IAudioRecorder, AudioListener {

    private val job = Job()
    private val api = DiarizationService(STCSpeechKit.diarizationService)
    private val audioRecorder: AudioRecorder = AudioRecorder()
    private var session: String? = null

    companion object {
        private val TAG = RestApiDiarization::class.java.simpleName
    }

    class Builder(diarizationListener: DiarizationListener) {
        private val diarizationListener = diarizationListener
        private var audioSource: Int = AUDIO_SOURCE
        private var audioEncoding: Int = AUDIO_ENCODING
        private var sampleRate: Int = 8000
        private var channels: Int = CHANNELS

        fun audioSource(audioSource: Int) = apply { this.audioSource = audioSource }
        fun audioEncoding(audioEncoding: Int) = apply { this.audioEncoding = audioEncoding }
        fun sampleRate(sampleRate: Int) = apply { this.sampleRate = sampleRate }
        fun channels(channels: Int) = apply { this.channels = channels }

        fun build() = RestApiDiarization(
                diarizationListener,
                audioSource,
                audioEncoding,
                sampleRate,
                channels
        )
    }

    private suspend fun startSession(): String? {
        var sessionId: String? = null
        val response = api.startSession(username, password, domainId)
        when {
            response.isSuccessful -> sessionId = response.body()?.sessionId
        }

        return sessionId
    }

    private suspend fun checkSession(sessionId: String): Boolean {
        var isOpenSession = false
        val response = api.checkSession(sessionId)
        when {
            response.code() == 200 -> isOpenSession = true
        }

        return isOpenSession
    }

    private suspend fun closeSession(sessionId: String) {
        api.closeSession(sessionId)
    }

    private suspend fun diarization(sessionId: String, voice: ByteArray): Data? {
        var result: Data?
        val response = api.sendVoiceToDiarization(sessionId, DiarizationRequest(Audio(voice, "audio/s16be")))
        when {
            response.isSuccessful -> result = response.body()?.data
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
    fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null
        audioRecorder.cancel()

        session?.let {
            launch(UI) {
                closeSession(it)
            }
        }

        job.cancel()
    }

    /**
     * Start recording (need to request permission in realtime)
     */
    override fun startRecording() {
        Logger.print(TAG, "start recording...")
        audioRecorder.start(audioSource, sampleRate, channels, audioEncoding)
        audioRecorder.setAudioListener(this)
        audioRecorder.isStreamingMode = false
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

        launch(job) {
            try {
                when {
                    session == null || !checkSession(session!!) -> session = startSession()
                }

                val wav = AudioConverter.rawToWave(STCSpeechKit.applicationContext, voice, sampleRate)

                val data = diarization(session!!, wav!!)

                data?.let {
                    launch(UI) {
                        listener?.onDiarizationResult(it)
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                throwable.message?.let {
                    launch(UI) {
                        listener?.onRecordingError(it)
                    }
                }

            }
        }
    }

    override fun onCancel() {
        Logger.print(TAG, "onCancel")
        launch(UI) {
            listener?.onRecordingCancel()
        }
    }

}