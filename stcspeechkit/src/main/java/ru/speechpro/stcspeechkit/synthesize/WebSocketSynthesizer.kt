package ru.speechpro.stcspeechkit.synthesize

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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
import ru.speechpro.stcspeechkit.domain.models.StreamSynthesizeRequest
import ru.speechpro.stcspeechkit.domain.models.Text
import ru.speechpro.stcspeechkit.synthesize.listeners.SynthesizerListener
import ru.speechpro.stcspeechkit.util.Logger
import java.util.concurrent.CountDownLatch

/**
 * WebSocketSynthesizer class contains methods for synthesize stream API
 *
 * @author Alexander Grigal
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class WebSocketSynthesizer private constructor(
    private var listener: SynthesizerListener?,
    private val language: Language,
    private val speaker: String,
    private val audioTrack: AudioTrack

) : BaseSynthesizer() {

    /**
     * Инициализация и использование ws в данный момент из разных потоков
     * */
    @Volatile
    private var ws: WebSocket? = null
    private var transaction: String? = null

    var initSignal = CountDownLatch(1)

    companion object {
        private val TAG = WebSocketSynthesizer::class.java.simpleName
    }

    class Builder(synthesizerListener: SynthesizerListener) {
        private val synthesizerListener = synthesizerListener
        private var language: Language = Language.Russian
        private var speaker: String = "Alexander"
        private var sampleRateInHz = 22050
        private var channelConfig = AudioFormat.CHANNEL_OUT_MONO
        private var audioFormat = AudioFormat.ENCODING_PCM_16BIT
        private var bufferSizeInBytes = 4096

        fun language(language: Language) = apply { this.language = language }
        fun speaker(speaker: String) = apply { this.speaker = speaker }
        fun sampleRateInHz(sampleRateInHz: Int) = apply { this.sampleRateInHz = sampleRateInHz }
        fun channelConfig(channelConfig: Int) = apply { this.channelConfig = channelConfig }
        fun audioFormat(audioFormat: Int) = apply { this.audioFormat = audioFormat }
        fun bufferSizeInBytes(bufferSizeInBytes: Int) = apply { this.bufferSizeInBytes = bufferSizeInBytes }

        fun build() = WebSocketSynthesizer(
            synthesizerListener,
            language,
            speaker,
            AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig,
                audioFormat, bufferSizeInBytes, AudioTrack.MODE_STREAM)
        )
    }

    private suspend fun openStream(sessionId: String, request: StreamSynthesizeRequest): Pair<String, String> {
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

    /** почему метод приватный и не вызывается? */
    private suspend fun closeStream(sessionId: String, transactionId: String): String? {
        val response = api.closeStream(sessionId, transactionId)
        when {
            response.isSuccessful -> return response.body()?.transactionId
            else -> return null
        }
    }

    private fun prepare(webSocketListener: WebSocketState) {
        Logger.print(TAG, "prepare")

        GlobalScope.launch(job) {
            try {
                if (session == null || !!checkSession(session!!)) {
                    session = startSession()
                    if (session != null) {
                        Logger.print(TAG, session!!)

                        if (!isSupportedLanguage(session!!, language)) {
                            Logger.print(TAG, LANGUAGE_IS_NOT_SUPPORTED)
                            GlobalScope.launch(Dispatchers.Main) {
                                listener?.onError(LANGUAGE_IS_NOT_SUPPORTED)
                            }
                            return@launch
                        }

                        if (!containsSpeakerForLanguage(session!!, language, speaker)) {
                            Logger.print(TAG, SPEAKER_IS_NOT_SUPPORTED)
                            GlobalScope.launch(Dispatchers.Main) {
                                listener?.onError(SPEAKER_IS_NOT_SUPPORTED)
                            }
                            return@launch
                        }

                        val result = openStream(session!!, StreamSynthesizeRequest(speaker, Text(TEXT_MIME_TYPE, null), AUDIO_ENDIANNESS))
                        transaction = result.first
                        val uri = result.second

                        audioTrack.play()

                        initWebSocket(uri, webSocketListener)
                        initSignal.countDown()
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

    private fun initWebSocket(uri: String, webSocketListener: WebSocketState) {
        try {
            ws = WebSocketFactory()
                .setConnectionTimeout(WEB_SOCKET_CONNECTION)
                .createSocket(uri)
                .addListener(object : WebSocketAdapter() {
                    override fun onConnected(websocket: WebSocket?, headers: MutableMap<String, MutableList<String>>?) {
                        super.onConnected(websocket, headers)
                        Logger.print(TAG, "onConnected")
                        webSocketListener.isReady()
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

                            if (frame?.closeCode != 1000) {
                                var reason = frame?.closeReason
                                if (reason == null) {
                                    reason = "Unknown error"
                                }

                                GlobalScope.launch(Dispatchers.Main) {
                                    listener?.onError(reason)
                                }
                            }

                    }

                    override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                        super.onConnectError(websocket, exception)
                        Logger.print(TAG, "onConnectError")
                        when {
                            exception != null -> {
                                Logger.withCause(TAG, exception)
                                GlobalScope.launch(Dispatchers.Main) {
                                    listener?.onError(exception.localizedMessage)
                                }

                            }
                        }
                    }

                    override fun onTextMessage(websocket: WebSocket?, text: String?) {
                        super.onTextMessage(websocket, text)
                        Logger.print(TAG, "onTextMessage: $text")
                    }

                    override fun onBinaryMessage(websocket: WebSocket?, binary: ByteArray?) {
                        super.onBinaryMessage(websocket, binary)
                        Logger.print(TAG, """${binary.toString()} size: ${binary!!.size}""")

                        audioTrack.write(binary, 0, binary.size)

                        GlobalScope.launch(Dispatchers.Main) {
                            listener?.onSynthesizerResult(binary)
                        }
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect()

        } catch (ex: WebSocketException) {
            Logger.withCause(TAG, ex)
            ex.message?.let {
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

        // опасаемся Exception - приведет к крашу приложения
        try {
            ws?.disconnect()
        } catch (throwable: Throwable) {
            Logger.withCause(TAG, throwable)
        }

        // нужно освобождать ресурсы иначе они заканчиваются, и аудио перестает воспроизводиться!
        releaseAudio()
        super.destroy()
    }

    private fun releaseAudio() {
        try {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.pause()
                audioTrack.flush()
            }
            audioTrack.release()
        } catch (ex: Exception) {
            Logger.withCause(TAG, ex)
        }
    }

    /**
     * Synthesis of voice
     *
     * @param text text for synthesis
     */
    fun synthesize(text: String) {
        Logger.print(TAG, "synthesize: $text")

        when {
            ws != null && ws!!.isOpen -> {
                Logger.print(TAG, "WebSocket is open")
                sendTextSafe(text)
            }
            else -> {
                Logger.print(TAG, "WebSocket is not open")
                prepare(object : WebSocketState {
                    override fun isReady() {
                        // Иногда ws не успевает доинициализироваться к данному моменту
                        // и равен null -> NullPointerException
                        initSignal.await()
                        sendTextSafe(text)
                    }
                })
            }
        }
    }

    private fun sendTextSafe(text: String) {
        try {
            ws!!.sendText(text)
        } catch (throwable: Throwable) {
            Logger.withCause(TAG, throwable)
            GlobalScope.launch(Dispatchers.Main) {
                listener?.onError(throwable.message.orEmpty())
            }
        }
    }

    private interface WebSocketState {
        fun isReady()
    }

    /**
     * Костыль.
     * Аналогично WebSocketRecognizer - избегаем конкурентного доступа к переменным класа
     * при нескольких быстрых вызовах synthesize(text: String),
     * или быстрого вызова destroy() после первого synthesize(text: String)
     *
     * Избавляюсь от данной проблемы используя для исполнения обоих методов actor-корутину.
     *   (команды выполняются строго последовательно в порядке вызова в одной и той же корутине)
     * */
    inner class StartStopHelper {
        private val scopeExceptionHandler = CoroutineExceptionHandler{ _, ex -> handleError(ex) }
        private val modelScope = CoroutineScope(job + scopeExceptionHandler)
        @SuppressLint("MissingPermission")
        private var controlActor = modelScope.actor<ControlCommand>(capacity = Channel.UNLIMITED) {
            for (command in channel) {
                try {
                    when (command) {
                        is SynthesizeCommand -> {
                            this@WebSocketSynthesizer.synthesize(command.text)
                            // обязательно ждем завершения
                            lastChildJob?.join()
                        }
                        is DestroyCommand -> {
                            this@WebSocketSynthesizer.destroy()
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

        /** Синтезировать текст */
        fun synthesize(text: String) = GlobalScope.launch(Dispatchers.Main) {
            if(!controlActor.isClosedForSend) {
                controlActor.send(SynthesizeCommand(text))
            }
        }
        /** Уничтожить объект и освободить ресурсы */
        fun destroy() = GlobalScope.launch(Dispatchers.Main) {
            if(!controlActor.isClosedForSend) {
                controlActor.send(DestroyCommand)
            }
            controlActor.close()
        }
    }
    val controlHelper = StartStopHelper()
    private var lastChildJob: Job? = null
}
sealed class ControlCommand
private class SynthesizeCommand(val text: String): ControlCommand()
private object DestroyCommand: ControlCommand()
