package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class Data(
    @JsonProperty("speakers")
    var speakers: List<SpeakersItem>?
)