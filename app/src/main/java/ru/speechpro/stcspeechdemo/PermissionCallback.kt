package ru.speechpro.stcspeechdemo

import java.util.ArrayList

/**
 * @author Alexander Grigal
 */
interface PermissionCallback {

    fun permissionGranted(requestCode: Int)

    fun partialPermissionGranted(requestCode: Int, grantedPermissions: ArrayList<String>)

    fun permissionDenied(requestCode: Int)

    fun neverAskAgain(requestCode: Int)

}
