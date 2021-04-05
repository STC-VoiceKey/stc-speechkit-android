package ru.speechpro.stcspeechkit.recognize

import android.annotation.SuppressLint
import com.neovisionaries.ws.client.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import ru.speechpro.stcspeechkit.common.*
import ru.speechpro.stcspeechkit.domain.models.StreamRecognizeRequest
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.media.AudioListener
import ru.speechpro.stcspeechkit.media.AudioRecorder
import ru.speechpro.stcspeechkit.recognize.listeners.RecognizerListener
import ru.speechpro.stcspeechkit.util.Logger
import java.lang.Exception

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
        Logger.print(TAG, "openStream Response: $response")
        when {
            response.isSuccessful -> {
                transactionId = response.headers()["X-Transaction-Id"]!!
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

                            GlobalScope.launch(Dispatchers.Main) {
                                listener?.onError(reason)
                            }
                        }

                        override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                            super.onConnectError(websocket, exception)
                            exception?.let { Logger.withCause(TAG, it) }
                            GlobalScope.launch(Dispatchers.Main) {
                                exception?.let { listener?.onError(it.error.name) }
                            }
                        }

                        override fun onTextMessage(websocket: WebSocket?, text: String?) {
                            super.onTextMessage(websocket, text)
                            text?.let { Logger.print(TAG, it) }
                            GlobalScope.launch(Dispatchers.Main) {
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
                GlobalScope.launch(Dispatchers.Main) {
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

        // Не уверен в актуальности данного исправления, т.к. в данный момент для каждой итераций
        // распознавания мы создаем новый экзмепляр RestApiSynthesizer
        // (отдельно пересоздавать job в таком сценарии нет необходимости)
        if (job.isCancelled) {
            job = Job()
        }

        lastChildJob = GlobalScope.launch(job) {
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
                GlobalScope.launch(Dispatchers.Main) {
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

        lastChildJob = GlobalScope.launch(job) {
            try {
                when {
                    session != null && transaction != null -> {
                        val response = closeStream(session!!, transaction!!)
                        transaction = null
                        response?.let {
                            GlobalScope.launch(Dispatchers.Main) {
                                listener?.onRecognizerTextResult(it)
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                Logger.withCause(TAG, throwable)
                GlobalScope.launch(Dispatchers.Main) {
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
        Logger.print(TAG, "onVoiceStream: $stream")
        when {
            ws != null -> ws!!.sendBinary(stream)
        }
    }

    override fun onStop(voice: ByteArray?) {
        Logger.print(TAG, "onStop: $voice")
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onRecordingStop()
        }
    }

    override fun onCancel() {
        Logger.print(TAG, "onCancel")
        GlobalScope.launch(Dispatchers.Main) {
            listener?.onRecordingCancel()
        }
    }

    /**
     * Костыль.
     * Суть проблемы:
     *  Старт/Стоп записи происходит запуском (launch) корутины
     *  Соответственно, последовательный быстрый запуск startRecording(), а затем stopRecording()
     *   начинают конкурировать между собой, краш приложения.
     *
     * Избавляюсь от данной проблемы используя для исполнения обоих методов actor-корутину.
     *   (команды выполняются строго последовательно в порядке вызова в одной и той же корутине)
     *
     *  Сделано чтобы работало, а не чтобы было красиво архитектурно.
     *  Мы старались делать минимальные вмешательства в код библиотеки.
     * */
    inner class StartStopHelper {
        private val scopeExceptionHandler = CoroutineExceptionHandler{ _, ex -> handleError(ex) }
        private val modelScope = CoroutineScope(job + scopeExceptionHandler)
        @SuppressLint("MissingPermission")
        private var controlActor = modelScope.actor<ControlCommand>(capacity = Channel.UNLIMITED) {
            for (command in channel) {
                try {
                    when (command) {
                        is StartRecordingCommand -> {
                            this@WebSocketRecognizer.startRecording()
                            // обязательно ждем завершения
                            lastChildJob?.join()
                        }
                        is StopRecordingCommand -> {
                            this@WebSocketRecognizer.stopRecording()
                            // обязательно ждем завершения
                            lastChildJob?.join()
                        }
                        is DestroyCommand -> {
                            this@WebSocketRecognizer.destroy()
                        }
                    }
                } catch (ex: Exception) { handleError(ex) }
            } // for
        }

        private fun handleError(ex: Throwable) {
            GlobalScope.launch(Dispatchers.Main) {
                if (ex !is CancellationException) {
                    listener?.onError(ex.message.orEmpty())
                }
            }
        }

        fun startRecording() = GlobalScope.launch(Dispatchers.Main) {
            if(!controlActor.isClosedForSend) {
                controlActor.send(StartRecordingCommand)
            }
        }
        fun stopRecording() = GlobalScope.launch(Dispatchers.Main) {
            if(!controlActor.isClosedForSend) {
                controlActor.send(StopRecordingCommand)
            }
        }
        fun destroy() = GlobalScope.launch(Dispatchers.Main) {
            if(!controlActor.isClosedForSend) {
                controlActor.send(DestroyCommand)
            }
            controlActor.close()
        }
    }
    val controlHelper = StartStopHelper()
    var lastChildJob: Job? = null
}
sealed class ControlCommand
object StartRecordingCommand: ControlCommand()
object StopRecordingCommand: ControlCommand()
object DestroyCommand: ControlCommand()
