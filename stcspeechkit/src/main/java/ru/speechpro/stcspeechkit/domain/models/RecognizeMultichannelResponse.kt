package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Victoria Prusakova
 */
data class RecognizeMultichannelResponse(
    @JsonProperty("channel_id")
    val channel: Int,
    @JsonProperty("text")
    val results: List<RecognizeWordResponse>
)