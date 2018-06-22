package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class RecognizeWordResponse(
    @JsonProperty("score")
    var score: String,
    @JsonProperty("length")
    var length: String,
    @JsonProperty("word")
    var word: String,
    @JsonProperty("begin")
    var begin: String = ""
)