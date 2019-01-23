package ru.speechpro.stcspeechdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment.*
import ru.speechpro.stcspeechkit.STCSpeechKit
import ru.speechpro.stcspeechkit.anti_spoofing.AntiSpoofingListener
import ru.speechpro.stcspeechkit.anti_spoofing.RestApiAntiSpoofing
import ru.speechpro.stcspeechkit.domain.models.AntiSpoofingResponse
import ru.speechpro.stcspeechkit.util.Logger


/**
 * @author Alexander Grigal
 */
class AntiSpoofingFragment : Fragment(), AntiSpoofingListener {

    private var antispoofing: RestApiAntiSpoofing? = null
    private var isRecording: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.print(TAG, "onViewCreated")

        textView.visibility = View.VISIBLE
        editText.visibility = View.GONE

        rbWebSocket.isEnabled = false
        rbRestApi.isEnabled = false

        val login = (activity as MainActivity).login
        val password = (activity as MainActivity).password
        val domainId = (activity as MainActivity).domainID

        STCSpeechKit.init(activity!!.applicationContext, login, password, domainId.toInt())
        STCSpeechKit.setLogging(true)

        antispoofing = RestApiAntiSpoofing.Builder(this)
                .build()

        btnAction.setOnClickListener {
            when {
                !isRecording -> {
                    onClickStart()
                }
                else -> {
                    onClickStop()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onClickStart() {
        antispoofing?.startRecording()
        btnAction.text = getText(R.string.stop)
        textView.text = ""
        isRecording = true
    }

    private fun onClickStop() {
        antispoofing?.stopRecording()
        btnAction.text = getText(R.string.start)
        isRecording = false
    }

    override fun onDetach() {
        super.onDetach()
        Logger.print(TAG, "onDetach")
        antispoofing?.destroy()
    }

    companion object {
        private val TAG = AntiSpoofingFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = AntiSpoofingFragment()
    }

    override fun onAntiSpoofingResult(result: AntiSpoofingResponse) {
        textView.text = result.toString()
    }

    override fun onRecordingStart() {
        //TODO("not implemented")
    }

    override fun onPowerDbUpdate(db: Short) {
        //TODO("not implemented")
    }

    override fun onRecordingStop() {
        //TODO("not implemented")
    }

    override fun onRecordingCancel() {
        //TODO("not implemented")
    }

    override fun onRecordingError(message: String) {
        Toast.makeText(context, "onRecordingError: $message", Toast.LENGTH_LONG).show()
    }

    override fun onError(message: String) {
        Toast.makeText(context, "onError: $message", Toast.LENGTH_LONG).show()
    }

}
