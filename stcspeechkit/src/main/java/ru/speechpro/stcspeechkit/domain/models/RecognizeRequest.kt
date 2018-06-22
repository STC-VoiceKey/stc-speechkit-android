package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class RecognizeRequest @JsonCreator
constructor(
    @JsonProperty("audio")
    val audio: Audio,
    @param:JsonProperty(value = "package_id", required = true)
    @field:JsonProperty(value = "package_id", required = true)
    val packageId: String
)