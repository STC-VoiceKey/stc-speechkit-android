package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE)
data class StartSessionRequest @JsonCreator
constructor(
    @param:JsonProperty(value = "username", required = true) @field:JsonProperty(value = "username", required = true)
    val username: String,
    @param:JsonProperty(value = "password", required = true) @field:JsonProperty(value = "password", required = true)
    val password: String,
    @param:JsonProperty(value = "domain_id", required = true) @field:JsonProperty(value = "domain_id", required = true)
    val domainId: Int
)
