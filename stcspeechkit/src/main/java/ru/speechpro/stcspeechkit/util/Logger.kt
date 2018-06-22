package ru.speechpro.stcspeechkit.util

import android.util.Log
import ru.speechpro.stcspeechkit.BuildConfig

/**
 * Logger class contains methods for printing to the console
 *
 * @author Alexander Grigal
 */
object Logger {

    /**
     * Enable logging
     */
    var isEnabled: Boolean = false

    /**
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message you would like logged.
     */
    fun print(tag: String, message: String): Logger {
        if (BuildConfig.DEBUG || isEnabled) {
            Log.println(Log.DEBUG, tag, message)
        }
        return this
    }

    /**
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param cause An throwable to log
     */
    fun withCause(tag: String, cause: Throwable) {
        if (BuildConfig.DEBUG || isEnabled) {
            Log.println(Log.ERROR, tag, Log.getStackTraceString(cause))
        }
    }

    /**
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param cause An exception to log
     */
    fun withCause(tag: String, cause: Exception) {
        if (BuildConfig.DEBUG || isEnabled) {
            Log.println(Log.ERROR, tag, Log.getStackTraceString(cause))
        }
    }

    /**
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param message The message you would like logged.
     * @param cause An exception to log
     */
    fun withCause(tag: String, message: String, cause: Exception) {
        if (BuildConfig.DEBUG || isEnabled) {
            Log.println(Log.ERROR, tag, message + Log.getStackTraceString(cause))
        }
    }
}
