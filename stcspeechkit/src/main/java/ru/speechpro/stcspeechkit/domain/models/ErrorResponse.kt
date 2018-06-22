package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class ErrorResponse(
    @JsonProperty("reason")
    var reason: String,
    @JsonProperty("message")
    var message: String
)