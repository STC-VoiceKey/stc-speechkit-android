package ru.speechpro.stcspeechkit.anti_spoofing

import com.fasterxml.jackson.databind.ObjectMapper
import com.speechpro.android.session.session_library.exception.InternetConnectionException
import com.speechpro.android.session.session_library.exception.RestException
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.common.AUDIO_ENCODING
import ru.speechpro.stcspeechkit.common.AUDIO_SOURCE
import ru.speechpro.stcspeechkit.common.CHANNELS
import ru.speechpro.stcspeechkit.domain.models.AntiSpoofingResponse
import ru.speechpro.stcspeechkit.domain.models.DataRequest
import ru.speechpro.stcspeechkit.domain.models.ErrorResponse
import ru.speechpro.stcspeechkit.domain.service.AntiSpoofingService
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.util.AudioConverter
import ru.speechpro.stcspeechkit.util.Logger


/**
 * RestApiAntiSpoofing class contains methods for anti spoofing REST API
 *
 * @author Alexander Grigal
 */

class RestApiAntiSpoofing private constructor(
        private var listener: AntiSpoofingListener?,
        private val audioSource: Int,
        private val audioEncoding: Int,
        private val sampleRate: Int,
        private val channels: Int

) : IAudioRecorder, AudioListener {

    private val job = Job()
    private val api = AntiSpoofingService(STCSpeechKit.antiSpoofingService, STCSpeechKit.sessionClient)
    private val audioRecorder: AudioRecorder = AudioRecorder()
    private var session: String? = null

    companion object {
        private val TAG = RestApiAntiSpoofing::class.java.simpleName
    }

    class Builder(antiSpoofingListener: AntiSpoofingListener) {
        private val antiSpoofingListener = antiSpoofingListener
        private var audioSource: Int = AUDIO_SOURCE
        private var audioEncoding: Int = AUDIO_ENCODING
        private var sampleRate: Int = 8000
        private var channels: Int = CHANNELS

        fun audioSource(audioSource: Int) = apply { this.audioSource = audioSource }
        fun audioEncoding(audioEncoding: Int) = apply { this.audioEncoding = audioEncoding }
        fun sampleRate(sampleRate: Int) = apply { this.sampleRate = sampleRate }
        fun channels(channels: Int) = apply { this.channels = channels }

        fun build() = RestApiAntiSpoofing(
                antiSpoofingListener,
                audioSource,
                audioEncoding,
                sampleRate,
                channels
        )
    }

    private suspend fun startSession(): String? {
        return api.startSession(STCSpeechKit.username, STCSpeechKit.password, STCSpeechKit.domainId)
    }

    private suspend fun checkSession(sessionId: String): Boolean {
        return api.checkSession(sessionId)
    }

    private suspend fun closeSession(sessionId: String) {
        api.closeSession(sessionId)
    }

    private suspend fun antiSpoofing(sessionId: String, voice: ByteArray): AntiSpoofingResponse? {
        val result: AntiSpoofingResponse
        val response = api.sendVoiceToAntiSpoofing(sessionId, DataRequest(voice))
        when {
            response.isSuccessful -> result = response.body()!!
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
            GlobalScope.launch(Dispatchers.Main) {
                closeSession(it)
            }
        }

        job.cancel()
    }

    private fun launchCoroutine(voice: ByteArray, hasWav: Boolean) {
        GlobalScope.launch(job) {
            try {
                when {
                    session == null || !checkSession(session!!) -> session = startSession()
                }

                val result = when {
                    hasWav -> antiSpoofing(session!!, voice)
                    else -> antiSpoofing(session!!, AudioConverter.rawToWave(STCSpeechKit.applicationContext, voice, sampleRate))
                }

                result?.let {
                    launch(Dispatchers.Main) {
                        listener?.onAntiSpoofingResult(it)
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                when (throwable) {
                    is InternetConnectionException, is RestException -> {
                        Logger.withCause(TAG, throwable)
                        throwable.message?.let {
                            launch(Dispatchers.Main) {
                                listener?.onError(it)
                            }
                        }
                    }
                    else -> {
                        throwable.message?.let {
                            launch(Dispatchers.Main) {
                                listener?.onRecordingError(it)
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * ByteArray Recognition
     *
     * @param ByteArray WAV data
     */
    fun antiSpoofing(voice: ByteArray) {
        Logger.print(TAG, "diarization file $voice")

        if (voice.size > 1048576 * 3) { // 1 Megabyte = 1048576 Bytes
            GlobalScope.launch(Dispatchers.Main) {
                listener?.onError("ByteArray is too big. Max. 3 MB per byte array.")
            }
            return
        }

        launchCoroutine(voice, true)
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
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onRecordingStart()
        }
    }

    override fun onProcess(amplitude: Short) {
        Logger.print(TAG, "onProcess: $amplitude")
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onPowerDbUpdate(amplitude)
        }
    }

    override fun onVoiceStream(stream: ByteArray?) {
        //not use in rest API
    }

    override fun onStop(voice: ByteArray?) {
        Logger.print(TAG, "onStop: $voice")
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onRecordingStop()
        }

        voice?.let { launchCoroutine(it, false) }
    }

    override fun onCancel() {
        Logger.print(TAG, "onCancel")
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onRecordingCancel()
        }
    }

}