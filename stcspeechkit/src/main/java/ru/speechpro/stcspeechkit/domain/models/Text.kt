package ru.speechpro.stcspeechkit.domain.models

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @author Alexander Grigal
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Text(
        val mime: String,
        val value: String?)


