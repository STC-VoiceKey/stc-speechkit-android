package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * @author Alexander Grigal
 */
data class AntiSpoofingResponse(
        @JsonProperty("decision")
        val decision: String
)