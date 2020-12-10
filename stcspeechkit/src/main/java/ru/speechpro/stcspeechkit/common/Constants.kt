@file:JvmName("Constants")

package ru.speechpro.stcspeechkit.common

import android.media.AudioFormat
import android.media.MediaRecorder

/**
 * @author Alexander Grigal
 */

// URL
const val SESSION_BASE_URL = "https://cp.speechpro.com/vksession/rest/"
const val BASE_URL = "https://cp.speechpro.com/"
// Audio
const val AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION
const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
const val CHANNELS = AudioFormat.CHANNEL_IN_MONO
const val AUDIO_ENDIANNESS = "audio/s16le"
// Package
const val AUDIO_MIME_TYPE = "audio/l16"
const val TEXT_MIME_TYPE = "text/plain"
const val PACKAGE_COMMON_PACKAGE = "FarField"
const val PACKAGE_ADVANCED = "FarField"
// WebSocket
const val WEB_SOCKET_CONNECTION = 5000
// Error message
const val LANGUAGE_IS_NOT_SUPPORTED = "This language is not supported"
const val SPEAKER_IS_NOT_SUPPORTED = "This speaker is not supported"


