package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class Audio(
    @JsonProperty("data")
    val data: ByteArray,
    @JsonProperty("mime")
    val mime: String
)