package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class DiarizationResponse(
    @JsonProperty("speakers")
    var speakers: List<SpeakersItem>?
)