package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class SessionIdResponse(
    @JsonProperty(value = "session_id")
    var sessionId: String = ""
)
