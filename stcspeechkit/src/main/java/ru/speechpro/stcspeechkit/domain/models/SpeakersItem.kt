package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class SpeakersItem(
    @JsonProperty("number")
    var number: Int,
    @JsonProperty("segments")
    var segments: List<SegmentsItem>?
)