package ru.speechpro.stcspeechkit.recognize

import android.support.annotation.RequiresPermission
import com.neovisionaries.ws.client.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.common.*
import ru.speechpro.stcspeechkit.domain.models.StreamRecognizeRequest
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.recognize.listeners.RecognizerListener
import ru.speechpro.stcspeechkit.util.Logger

/**
 * WebSocketRecognizer class contains methods for recognize API
 *
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class WebSocketRecognizer private constructor(
        private var listener: RecognizerListener?,
        private val audioSource: Int,
        private val audioEncoding: Int,
        private val sampleRate: Int,
        private val channels: Int,
        private val packageId: String

) : BaseRecognizer(), IAudioRecorder, AudioListener {

    private val audioRecorder: AudioRecorder = AudioRecorder()

    private var ws: WebSocket? = null
    private var transaction: String? = null

    companion object {
        private val TAG = WebSocketRecognizer::class.java.simpleName
    }

    class Builder(recognizeListener: RecognizerListener) {
        private val recognizerListener = recognizeListener
        private var audioSource: Int = AUDIO_SOURCE
        private var audioEncoding: Int = AUDIO_ENCODING
        private var sampleRate: Int = 16000
        private var channels: Int = CHANNELS
        private var packageId: String = PACKAGE_COMMON_PACKAGE

        fun audioSource(audioSource: Int) = apply { this.audioSource = audioSource }
        fun audioEncoding(audioEncoding: Int) = apply { this.audioEncoding = audioEncoding }
        fun sampleRate(sampleRate: Int) = apply { this.sampleRate = sampleRate }
        fun channels(channels: Int) = apply { this.channels = channels }
        fun packageId(packageId: String) = apply { this.packageId = packageId }

        fun build() = WebSocketRecognizer(
                recognizerListener,
                audioSource,
                audioEncoding,
                sampleRate,
                channels,
                packageId
        )
    }

    private suspend fun openStream(sessionId: String, request: StreamRecognizeRequest): Pair<String, String> {
        var transactionId = ""
        var ws = ""
        val response = api.openStream(sessionId, request)
        when {
            response.isSuccessful -> {
                transactionId = response.headers().get("x-transaction-id")!!
                ws = response.body()!!.url
            }
        }
        return Pair(transactionId, ws)
    }

    private suspend fun closeStream(sessionId: String, transactionId: String): String? {
        val response = api.closeStream(sessionId, transactionId)
        when {
            response.isSuccessful && response.body() != null -> return response.body()!!.text
            else -> return null
        }
    }

    private fun initWebSocket(uri: String) {
        try {
            ws = WebSocketFactory()
                    .setConnectionTimeout(WEB_SOCKET_CONNECTION)
                    .createSocket(uri)
                    .addListener(object : WebSocketAdapter() {
                        override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
                            super.onConnected(websocket, headers)
                            Logger.print(TAG, "onConnected")
                        }

                        override fun onMessageError(websocket: WebSocket?, cause: WebSocketException?, frames: MutableList<WebSocketFrame>?) {
                            super.onMessageError(websocket, cause, frames)
                            cause?.let { Logger.withCause(TAG, it) }
                        }

                        override fun onDisconnected(websocket: WebSocket?, serverCloseFrame: WebSocketFrame?, clientCloseFrame: WebSocketFrame?, closedByServer: Boolean) {
                            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                            Logger.print(TAG, "onDisconnected")
                        }

                        override fun onCloseFrame(websocket: WebSocket?, frame: WebSocketFrame?) {
                            super.onCloseFrame(websocket, frame)
                            Logger.print(TAG, "onCloseFrame: $frame")

                            var reason = frame?.closeReason
                            if (reason == null) {
                                reason = "Unknown error"
                            }

                            launch(UI) {
                                listener?.onError(reason)
                            }
                        }

                        override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                            super.onConnectError(websocket, exception)
                            exception?.let { Logger.withCause(TAG, it) }
                            launch(UI) {
                                exception?.let { listener?.onError(it.error.name) }
                            }
                        }

                        override fun onTextMessage(websocket: WebSocket?, text: String?) {
                            super.onTextMessage(websocket, text)
                            text?.let { Logger.print(TAG, it) }
                            launch(UI) {
                                text?.let { listener?.onRecognizerTextMessage(it) }
                            }
                        }

                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect()

            audioRecorder.start(audioSource, sampleRate, channels, audioEncoding)
            audioRecorder.setAudioListener(this@WebSocketRecognizer)
            audioRecorder.isStreamingMode = true
        } catch (ex: WebSocketException) {
            ex.message?.let {
                Logger.print(TAG, it)
                launch(UI) {
                    listener?.onError(it)
                }
            }
        }
    }

    /**
     * It is necessary to call when the activity is destroyed
     */
    override fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null
        audioRecorder.cancel()
        ws?.disconnect()

        super.destroy()
    }

    /**
     * Start recording (need to request permission in realtime)
     */
    override fun startRecording() {
        Logger.print(TAG, "start recording...")

        launch(job) {
            try {
                when {
                    session == null || checkSession(session!!) -> session = startSession()
                }
                loadPackage(session!!, packageId)
                val result = openStream(session!!, StreamRecognizeRequest(AUDIO_MIME_TYPE, PACKAGE_COMMON_PACKAGE))

                transaction = result.first
                val uri = result.second

                initWebSocket(uri)

            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                launch(UI) {
                    listener?.onError(throwable.message!!)
                }
            }
        }
    }

    /**
     * Stop recording
     */
    override fun stopRecording() {
        Logger.print(TAG, "stop recording")
        audioRecorder.stop()

        launch(job) {
            try {
                when {
                    session != null && transaction != null -> {
                        val response = closeStream(session!!, transaction!!)
                        response?.let {
                            launch(UI) {
                                listener?.onRecognizerTextResult(it)
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                launch(UI) {
                    listener?.onError(throwable.message!!)
                }
            }
        }
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
        Logger.print(TAG, "onVoiceStream: $stream")
        when {
            ws != null -> ws!!.sendBinary(stream)
        }
    }

    override fun onStop(voice: ByteArray?) {
        Logger.print(TAG, "onStop: $voice")
        launch(UI) {
            listener?.onRecordingStop()
        }
    }

    override fun onCancel() {
        Logger.print(TAG, "onCancel")
        launch(UI) {
            listener?.onRecordingCancel()
        }
    }

}
