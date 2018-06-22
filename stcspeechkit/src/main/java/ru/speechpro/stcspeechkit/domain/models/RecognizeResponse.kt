package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class RecognizeResponse(
    @JsonProperty("score")
    val score: String,
    @JsonProperty("text")
    val text: String
)