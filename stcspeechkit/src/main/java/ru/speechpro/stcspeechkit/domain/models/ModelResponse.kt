package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Victoria Prusakova
 */
data class ModelResponse(
        @JsonProperty("model_id")
        var modelId: String = "",
        @JsonProperty("name")
        var name: String = "",
        @JsonProperty("language")
        var language: String = "",
        @JsonProperty("sample_rate")
        var sampleRate: Int = 0
)