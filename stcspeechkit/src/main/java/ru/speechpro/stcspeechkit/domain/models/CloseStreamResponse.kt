package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class CloseStreamResponse(
    @JsonProperty("transaction_id")
    var transactionId: String,
    @JsonProperty("synthesize_text_size")
    var synthesizeTextSize: Int
)