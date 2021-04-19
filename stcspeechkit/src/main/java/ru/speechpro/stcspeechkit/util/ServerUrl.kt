package ru.speechpro.stcspeechkit.util

import ru.speechpro.stcspeechkit.common.BASE_URL
import ru.speechpro.stcspeechkit.common.SESSION_BASE_URL
import java.util.*

object ServerUrl {

    fun getSessionUrl(): String {
        val properties = getProperties()
        val property = properties.getProperty("session_url")
        return property ?: SESSION_BASE_URL
    }


    fun getBaseUrl(): String {
        val properties = getProperties()
        val property = properties.getProperty("base_url")
        return property ?: BASE_URL
    }

    private fun getProperties(): Properties {
        val properties = Properties()
        val inputStream = javaClass.classLoader.getResourceAsStream("url.properties")
        inputStream?.let{properties.load(it)}
        return properties
    }
}