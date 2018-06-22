package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Alexander Grigal
 */
data class PackageResponse(
    @JsonProperty("loaded")
    var loaded: Boolean = false,
    @JsonProperty("sample_rate")
    var sampleRate: Int = 0,
    @JsonProperty("name")
    var name: String = "",
    @JsonProperty("language")
    var language: String = "",
    @JsonProperty("package_id")
    var packageId: String = ""
)