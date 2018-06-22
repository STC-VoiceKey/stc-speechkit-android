package ru.speechpro.stcspeechkit.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * AppInfo class contains methods for obtaining the version name and version code
 *
 * @author Alexander Grigal
 */
object AppInfo {

    /**
     * Getting the version name
     *
     * @param context Application context.
     * @return Version name
     */
    fun getVersionName(context: Context): String {
        var versionName = ""
        try {
            versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return versionName
    }

    /**
     * Getting the version code
     *
     * @param context Application context.
     * @return Version code
     */
    fun getVersionCode(context: Context): Int {
        var versionCode = 0
        try {
            versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return versionCode
    }
}
