package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Victoria Prusakova
 */
data class StartTransactionRequest @JsonCreator
constructor(
        @param:JsonProperty(value = "model_id", required = true) @field:JsonProperty(value = "model_id", required = true)
        val modelId: String,
        @param:JsonProperty(value = "recognition_config") @field:JsonProperty(value = "recognition_config")
        val config: RecognitionConfig? = null
)