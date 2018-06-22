package ru.speechpro.stcspeechkit.interfaces


/**
 * Implemented this in order for AudioRecorder to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface AudioRecorderListener {

    /**
     * Called when the record is started.
     */
    fun onRecordingStart()

    /**
     * Called when the power db is update.
     *
     * @param db
     */
    fun onPowerDbUpdate(db: Short)

    /**
     * Called when the record is stopped.
     */
    fun onRecordingStop()

    /**
     * Called when the record is canceled.
     */
    fun onRecordingCancel()

    /**
     * Called when an error occurred while writing.
     *
     * @param message error text
     */
    fun onRecordingError(message: String)

}