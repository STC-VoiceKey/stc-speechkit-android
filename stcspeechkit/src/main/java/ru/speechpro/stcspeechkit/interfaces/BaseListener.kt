package ru.speechpro.stcspeechkit.interfaces


/**
 * @author Alexander Grigal
 */
interface BaseListener {

    /**
     * Called when a error occurs.
     *
     * @param message error text
     */
    fun onError(message: String)

}