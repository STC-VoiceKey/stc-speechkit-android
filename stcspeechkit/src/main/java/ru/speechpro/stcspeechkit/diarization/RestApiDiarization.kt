package ru.speechpro.stcspeechkit.diarization

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.STCSpeechKit.domainId
import ru.speechpro.stcspeechkit.STCSpeechKit.password
import ru.speechpro.stcspeechkit.STCSpeechKit.username
import ru.speechpro.stcspeechkit.common.AUDIO_ENCODING
import ru.speechpro.stcspeechkit.common.AUDIO_SOURCE
import ru.speechpro.stcspeechkit.common.CHANNELS
import ru.speechpro.stcspeechkit.domain.models.*
import ru.speechpro.stcspeechkit.domain.service.DiarizationService
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.util.AudioConverter
import ru.speechpro.stcspeechkit.util.Logger
import java.io.IOException
import kotlin.reflect.KClass

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
    private val api = DiarizationService(STCSpeechKit.diarizationService, STCSpeechKit.sessionClient)
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
        return api.startSession(username, password, domainId)
    }

    private suspend fun checkSession(sessionId: String): Boolean {
        return api.checkSession(sessionId)
    }

    private suspend fun closeSession(sessionId: String) {
        api.closeSession(sessionId)
    }

    private suspend fun diarization(sessionId: String, voice: ByteArray): List<SpeakersItem>? {
        val result: List<SpeakersItem>?
        val response = api.sendVoiceToDiarization(sessionId, DiarizationRequest(voice))
        when {
            response.isSuccessful -> result = response.body()?.speakers
            else -> {
                val error = response.errorBody()?.string()
                val mapper = ObjectMapper()
                val errorResponse: ErrorResponse = try {
                    mapper.readValue(error, ErrorResponse::class.java)
                } catch (ioe: IOException) {
                    Logger.withCause(TAG, ioe)
                    throw Throwable(ioe)
                } catch (jpe: JsonParseException) {
                    Logger.withCause(TAG, jpe)
                    throw Throwable(jpe)
                } catch (jme: JsonMappingException) {
                    Logger.withCause(TAG, jme)
                    throw Throwable(jme)
                }
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
                try {
                    closeSession(it)
                } catch (ex: Exception) {
                    Logger.print(TAG, "RestApiAntiSpoofing $ex")
                }
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
                    hasWav -> diarization(session!!, voice)
                    else -> diarization(session!!, AudioConverter.rawToWave(STCSpeechKit.applicationContext, voice, sampleRate))
                }

                result?.let {
                    launch(Dispatchers.Main) {
                        listener?.onDiarizationResult(it)
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                launch(Dispatchers.Main) {
                    listener?.onRecordingError(throwable.message!!)
                }
            }
        }
    }

    /**
     * ByteArray Diarization
     *
     * @param ByteArray WAV data
     */
    fun diarization(voice: ByteArray) {
        Logger.print(TAG, "diarization file " + voice)

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