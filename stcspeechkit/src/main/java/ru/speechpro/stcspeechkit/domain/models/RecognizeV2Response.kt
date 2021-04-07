package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class RecognizeV2Response(
    @JsonProperty("score")
    val score: String,
    @JsonProperty("text")
    val text: String,
    @JsonProperty("interpretation")
    val interpretation: Map<String,String>?
)