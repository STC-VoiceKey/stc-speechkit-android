package ru.speechpro.stcspeechdemo

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast
import java.util.*

/**
 * @author Alexander Grigal
 */
class PermissionUtils(activity: Activity) {

    private val context: Context
    private val activity: Activity

    private val permissionCallback: PermissionCallback

    private var permissionList = ArrayList<String>()
    private var listPermissionsNeeded = ArrayList<String>()
    private var dialogContent = ""
    private var reqCode: Int = 0

    init {
        this.context = activity
        this.activity = activity

        this.permissionCallback = activity as PermissionCallback
    }


    /**
     * Check the API Level & Permission
     *
     * @param permissions
     * @param dialogContent
     * @param requestCode
     */
    fun checkPermission(permissions: ArrayList<String>, dialogContent: String, requestCode: Int) {
        this.permissionList = permissions
        this.dialogContent = dialogContent
        this.reqCode = requestCode

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions(permissions, requestCode)) {
                permissionCallback.permissionGranted(requestCode)
            }
        } else {
            permissionCallback.permissionGranted(requestCode)
        }

    }

    private fun checkAndRequestPermissions(permissions: ArrayList<String>, requestCode: Int): Boolean {
        when {
            permissions.size > 0 -> {
                listPermissionsNeeded = ArrayList()

                for (i in permissions.indices) {
                    val hasPermission = ContextCompat.checkSelfPermission(activity, permissions[i])

                    if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                        listPermissionsNeeded.add(permissions[i])
                    }

                }

                if (!listPermissionsNeeded.isEmpty()) {
                    ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray(), requestCode)
                    return false
                }
            }
        }

        return true
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.size > 0) {
                val perms = HashMap<String, Int>()

                for (i in permissions.indices) {
                    perms[permissions[i]] = grantResults[i]
                }

                val pendingPermissions = ArrayList<String>()

                for (i in listPermissionsNeeded.indices) {
                    if (perms[listPermissionsNeeded[i]] != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, listPermissionsNeeded[i]))
                            pendingPermissions.add(listPermissionsNeeded[i])
                        else {
                            permissionCallback.neverAskAgain(reqCode)
                            Toast.makeText(activity, R.string.go_to_settings, Toast.LENGTH_LONG).show()
                            return
                        }
                    }

                }

                when {
                    pendingPermissions.size > 0 -> showMessageOKCancel(dialogContent,
                            DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkPermission(permissionList, dialogContent, reqCode)
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        if (permissionList.size == pendingPermissions.size)
                                            permissionCallback.permissionDenied(reqCode)
                                        else
                                            permissionCallback.partialPermissionGranted(reqCode, pendingPermissions)
                                    }
                                }
                            })
                    else -> {
                        permissionCallback.permissionGranted(reqCode)
                    }
                }

            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity, R.style.AlertDialog)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, okListener)
                .create()
                .show()
    }

}
