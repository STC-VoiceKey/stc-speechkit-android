package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class DiarizationRequest(
    @JsonProperty("audio")
    var audio: Audio
)