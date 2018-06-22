package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class StreamSynthesizeRequest(
    @param:JsonProperty(value = "voice_name", required = true)
    @field:JsonProperty(value = "voice_name", required = true)
    val voiceName: String,
    @JsonProperty("text")
    val text: Text,
    @JsonProperty("audio")
    val audio: String = ""
)