package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class DataResponse(
    @JsonProperty("data")
    val data: ByteArray
)