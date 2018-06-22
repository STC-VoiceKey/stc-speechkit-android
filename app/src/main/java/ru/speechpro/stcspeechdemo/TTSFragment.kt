package ru.speechpro.stcspeechdemo


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_tts.*
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.synthesize.Language
import ru.speechpro.stcspeechkit.synthesize.RestApiSynthesizer
import ru.speechpro.stcspeechkit.synthesize.listeners.SynthesizerListener
import ru.speechpro.stcspeechkit.synthesize.WebSocketSynthesizer
import ru.speechpro.stcspeechkit.util.Logger

class TTSFragment : Fragment(), SynthesizerListener {

    private var webSocketSynthesizer: WebSocketSynthesizer? = null
    private var restApiSynthesizer: RestApiSynthesizer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login = (activity as MainActivity).login
        val password = (activity as MainActivity).password
        val domainId = (activity as MainActivity).domainID

        STCSpeechKit.init(activity!!.applicationContext, login, password, domainId.toInt())
        STCSpeechKit.setLogging(true)

        webSocketSynthesizer = WebSocketSynthesizer.Builder(this)
                .language(Language.Russian)
                .speaker("Alexander")
                .build()

        restApiSynthesizer = RestApiSynthesizer.Builder(this)
                .language(Language.Russian)
                .speaker("Alexander")
                .build()

        editText.requestFocus()

        btnAction.setOnClickListener {
            when {
                rbWebSocket.isChecked -> webSocketSynthesizer?.synthesize(editText.text.toString())
                else -> restApiSynthesizer?.synthesize(editText.text.toString())
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        Logger.print(TAG, "onDetach")
        webSocketSynthesizer?.destroy()
        restApiSynthesizer?.destroy()
    }

    companion object {
        private val TAG = TTSFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = TTSFragment()
    }

    override fun onSynthesizerResult(byteArray: ByteArray) {
        Toast.makeText(context, """onSynthesizerResult: $byteArray""", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toast.makeText(context, """onError: $message""", Toast.LENGTH_SHORT).show()
    }
}
