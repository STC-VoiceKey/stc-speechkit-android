package ru.speechpro.stcspeechdemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


/**
 * @author Alexander Grigal
 */
const val EXTRA_LOGIN = "ru.speechpro.stcspeechdemo.LOGIN"
const val EXTRA_PASSWORD = "ru.speechpro.stcspeechdemo.PASSWORD"
const val EXTRA_DOMAIN_ID = "ru.speechpro.stcspeechdemo.DOMAIN_ID"

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_LOGIN, editTextLogin.text.toString())
                putExtra(EXTRA_PASSWORD, editTextPassword.text.toString())
                putExtra(EXTRA_DOMAIN_ID, editTextDomain.text.toString())
            }
            startActivity(intent)
        }
    }
}