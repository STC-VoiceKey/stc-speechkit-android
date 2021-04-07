package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Victoria Prusakova
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RecognizeV2Request @JsonCreator
constructor(
    @JsonProperty("audio")
    val audio: Audio,
    @param:JsonProperty(value = "model_id", required = true) @field:JsonProperty(value = "model_id", required = true)
    val modelId: String,
    @param:JsonProperty(value = "recognition_config") @field:JsonProperty(value = "recognition_config")
    val config: RecognitionConfig? = null
)
