package ru.speechpro.stcspeechdemo

import android.Manifest
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.speechpro.stcspeechkit.util.Logger
import java.util.*

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, PermissionCallback {

    private val permissions = ArrayList<String>()
    private var permissionUtils: PermissionUtils? = null

    lateinit var login: String
    lateinit var password: String
    lateinit var domainID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login = intent.getStringExtra(EXTRA_LOGIN)
        password = intent.getStringExtra(EXTRA_PASSWORD)
        domainID = intent.getStringExtra(EXTRA_DOMAIN_ID)

        permissionUtils = PermissionUtils(this)
        permissions.add(Manifest.permission.RECORD_AUDIO)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, ASRFragment.newInstance())
                .commit()

        permissionUtils?.checkPermission(permissions, getString(R.string.give_necessary_permissions), 1)

    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_asr -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ASRFragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tts -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, TTSFragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_diarization -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, DiarizationFragment.newInstance())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)
    }

    override fun permissionGranted(requestCode: Int) {
        Logger.print(TAG, "permissionGranted $requestCode")
    }

    override fun partialPermissionGranted(requestCode: Int, grantedPermissions: ArrayList<String>) {
        Logger.print(TAG, "partialPermissionGranted $requestCode $grantedPermissions")
    }

    override fun permissionDenied(requestCode: Int) {
        Logger.print(TAG, "requestCode $requestCode")
        finish()
    }

    override fun neverAskAgain(requestCode: Int) {
        Logger.print(TAG, "neverAskAgain $requestCode")
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}
