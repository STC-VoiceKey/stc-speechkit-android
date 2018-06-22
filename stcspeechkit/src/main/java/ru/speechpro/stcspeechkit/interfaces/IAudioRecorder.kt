package ru.speechpro.stcspeechkit.interfaces

import android.support.annotation.RequiresPermission


/**
 * @author Alexander Grigal
 */
interface IAudioRecorder {

    @RequiresPermission("android.permission.RECORD_AUDIO")
    fun startRecording()

    fun stopRecording()

    fun cancelRecording()

}