package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class SegmentsItem(
    @JsonProperty("start")
    var start: Number,
    @JsonProperty("length")
    var length: Number
)