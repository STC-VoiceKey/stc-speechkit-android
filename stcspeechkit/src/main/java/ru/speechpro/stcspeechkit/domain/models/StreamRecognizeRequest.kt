package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class StreamRecognizeRequest(
    @JsonProperty("mime")
    var mime: String,
    @JsonProperty("package_id")
    @field:JsonProperty(value = "package_id", required = true)
    var packageId: String
)