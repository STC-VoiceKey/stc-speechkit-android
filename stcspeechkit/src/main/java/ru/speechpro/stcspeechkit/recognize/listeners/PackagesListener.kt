package ru.speechpro.stcspeechkit.recognize.listeners

import ru.speechpro.stcspeechkit.interfaces.BaseListener
import ru.speechpro.stcspeechkit.domain.models.PackageResponse


/**
 * Implemented this in order for PackageRecognizer to be able to callback in certain situations.
 *
 * @author Alexander Grigal
 */
interface PackagesListener : BaseListener {

    /**
     * Called when the list of available packeges is received.
     *
     * @param packages List<PackageResponse>?
     */
    fun onPackagesResult(packages: List<PackageResponse>?)

}