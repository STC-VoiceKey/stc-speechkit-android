package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Victoria Prusakova
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RecognitionConfig(

    @JsonProperty("additional_words")
    val additionalWords: List<String>,
    @JsonProperty("vocabulary_ids")
    val vocabularies: List<String>
)