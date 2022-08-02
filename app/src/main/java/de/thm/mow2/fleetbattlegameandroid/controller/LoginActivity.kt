package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.model.LoginModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.LoginController
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication

class LoginActivity : AppCompatActivity(), LoginController {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setTheme(R.style.Theme_FleetBattleGameAndroid)
        application.setTheme(R.style.Theme_FleetBattleGameAndroid)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_default)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)

        loginErrorTextView = findViewById(R.id.loginErrorText)

        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener(::onLoginClicked)

        registerRedirectButton = findViewById(R.id.registerButton)
        registerRedirectButton.setOnClickListener(::onRegisterRedirectClicked)

        model = LoginModel(this)

        loginErrors = resources.getStringArray(R.array.login_errors)
    }

    override fun onResume() {
        super.onResume()

        if (TPFirebaseAuthentication.isSignedIn()) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginErrorTextView: TextView
    private lateinit var loginButton: Button

    private lateinit var registerRedirectButton: Button

    private lateinit var model: LoginModel

    private lateinit var loginErrors: Array<String>

    private fun onLoginClicked(view: View) {
        val email = emailInput.text.toString()
        val pw = passwordInput.text.toString()

        if (email.isNotBlank() && pw.isNotBlank()) {
            model.onLoginClicked(email, pw)
        } else {
            loginErrorTextView.text = loginErrors[0]
        }
    }

    private fun onRegisterRedirectClicked(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    override fun redirectToMenuActivity() {
        loginErrorTextView.text = ""

        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    override fun showErrorMessage(message: String) {
        when {
            message.startsWith("There is no user record") -> loginErrorTextView.text =
                loginErrors[1]
            message.startsWith("The password is invalid") -> loginErrorTextView.text =
                loginErrors[2]
            message.startsWith("The email address is badly formatted") -> loginErrorTextView.text =
                loginErrors[3]
            else -> loginErrorTextView.text = message
        }
    }
}