package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * @author Alexander Grigal
 */
data class DataRequest(
        @JsonProperty("data")
        val data: ByteArray
)