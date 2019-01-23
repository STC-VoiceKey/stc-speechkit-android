package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Audio(
    @JsonProperty("data")
    val data: ByteArray,
    @JsonProperty("mime")
    val mime: String
)