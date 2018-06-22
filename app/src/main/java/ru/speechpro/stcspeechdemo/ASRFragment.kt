package ru.speechpro.stcspeechdemo


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_asr.*
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.interfaces.IAudioRecorder
import ru.speechpro.stcspeechkit.recognize.RestApiRecognizer
import ru.speechpro.stcspeechkit.recognize.WebSocketRecognizer
import ru.speechpro.stcspeechkit.recognize.listeners.RecognizerListener
import ru.speechpro.stcspeechkit.util.Logger

class ASRFragment : Fragment(), RecognizerListener {

    private var webSocketRecognizer: WebSocketRecognizer? = null
    private var restApiRecognizer: RestApiRecognizer? = null

    private var isRecording: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_asr, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.print(TAG, "onViewCreated")

        val login = (activity as MainActivity).login
        val password = (activity as MainActivity).password
        val domainId = (activity as MainActivity).domainID

        STCSpeechKit.init(activity!!.applicationContext, login, password, domainId.toInt())
        STCSpeechKit.setLogging(true)

        webSocketRecognizer = WebSocketRecognizer.Builder(this)
                .build()

        restApiRecognizer = RestApiRecognizer.Builder(this)
                .build()

        btnAction.setOnClickListener {
            when {
                !isRecording -> onClickStart(when {
                    rbWebSocket.isChecked -> webSocketRecognizer
                    else -> restApiRecognizer
                })
                else -> onClickStop(when {
                    rbWebSocket.isChecked -> webSocketRecognizer
                    else -> restApiRecognizer
                })
            }

        }

    }

    @SuppressLint("MissingPermission")
    private fun onClickStart(recognizer: IAudioRecorder?) {
        recognizer?.startRecording()
        textView.text = ""
        btnAction.text = getText(R.string.stop)
        isRecording = true
        rbWebSocket.isEnabled = false
        rbRestApi.isEnabled = false
    }

    private fun onClickStop(recognizer: IAudioRecorder?) {
        recognizer?.stopRecording()
        btnAction.text = getText(R.string.start)
        isRecording = false
        rbWebSocket.isEnabled = true
        rbRestApi.isEnabled = true
    }

    override fun onDetach() {
        super.onDetach()
        Logger.print(TAG, "onDetach")
        webSocketRecognizer?.destroy()
        restApiRecognizer?.destroy()
    }

    companion object {
        private val TAG = ASRFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = ASRFragment()
    }

    override fun onRecognizerTextMessage(result: String) {
        Logger.print(TAG, "onRecognizerTextMessage: $result")
        textView.text = result
    }

    override fun onRecognizerTextResult(result: String) {
        Logger.print(TAG, "onRecognizerTextResult: $result")
        textView.text = result
    }

    override fun onRecordingStart() {
        Logger.print(TAG, "onRecordingStart")
    }

    override fun onPowerDbUpdate(db: Short) {
        Logger.print(TAG, "onPowerDbUpdate $db")
    }

    override fun onRecordingStop() {
        Logger.print(TAG, "onRecordingStop")
    }

    override fun onRecordingCancel() {
        Logger.print(TAG, "onRecordingCancel")
    }

    override fun onRecordingError(message: String) {
        Logger.print(TAG, "onRecordingError $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Logger.print(TAG, "onError $message")
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
