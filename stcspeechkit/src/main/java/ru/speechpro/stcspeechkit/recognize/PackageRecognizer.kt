package ru.speechpro.stcspeechkit.recognize

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import ru.speechpro.stcspeechkit.recognize.listeners.PackagesListener
import ru.speechpro.stcspeechkit.util.Logger

/**
 * PackageRecognizer class contains methods for recognize API
 *
 * @author Alexander Grigal
 */
class PackageRecognizer constructor(
        private var listener: PackagesListener?

) : BaseRecognizer() {

    companion object {
        private val TAG = PackageRecognizer::class.java.simpleName
    }

    fun getAvailablePackages() {
        Logger.print(TAG, "getAvailablePackages")

        launch(job) {
            try {
                when {
                    session == null || !checkSession(session!!) -> session = startSession()
                }
                when {
                    session != null -> {
                        Logger.print(TAG, session!!)
                        val result = availablePackages(session!!)
                        result?.let {
                            launch(UI) {
                                listener?.onPackagesResult(it)
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
     * It is necessary to call when the activity is destroyed
     */
    override fun destroy() {
        Logger.print(TAG, "destroy")
        listener = null

        super.destroy()
    }
}
