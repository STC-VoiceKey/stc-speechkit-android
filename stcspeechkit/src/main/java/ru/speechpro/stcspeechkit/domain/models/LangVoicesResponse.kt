package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class LangVoicesResponse(
    @JsonProperty("gender")
    var gender: String,
    @JsonProperty("name")
    var name: String,
    @JsonProperty("id")
    var id: String
)