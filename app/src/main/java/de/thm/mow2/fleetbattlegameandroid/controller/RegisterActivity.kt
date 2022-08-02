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
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.model.RegisterModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.RegisterController

class RegisterActivity : AppCompatActivity(), RegisterController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_default)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        displayNameInput = findViewById(R.id.usernameInput)

        registerErrorTextView = findViewById(R.id.registerErrorText)

        registerButton = findViewById(R.id.registerButton)
        registerButton.setOnClickListener(::onRegisterClicked)

        loginRedirectButton = findViewById(R.id.loginButton)
        loginRedirectButton.setOnClickListener(::onLoginRedirectClicked)

        model = RegisterModel(this)

        registerErrors = resources.getStringArray(R.array.register_errors)
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
    private lateinit var displayNameInput: EditText
    private lateinit var registerErrorTextView: TextView
    private lateinit var registerButton: Button

    private lateinit var loginRedirectButton: Button

    private lateinit var model: RegisterModel

    private lateinit var registerErrors: Array<String>

    private fun onRegisterClicked(view: View) {
        val displayName = displayNameInput.text.toString()
        val email = emailInput.text.toString()
        val pw = passwordInput.text.toString()

        when {
            displayName.isBlank() -> showErrorMessage(registerErrors[0])
            displayName.contains(" ") -> showErrorMessage(registerErrors[1])
            email.isBlank() -> showErrorMessage(registerErrors[2])
            email.contains(" ") -> showErrorMessage(registerErrors[3])
            pw.isBlank() -> showErrorMessage(registerErrors[4])
            pw.contains(" ") -> showErrorMessage(registerErrors[5])
            displayName.length < 3 -> showErrorMessage(registerErrors[6])
            pw.length < 5 -> showErrorMessage(registerErrors[7])
            else -> model.onRegisterClicked(email, pw, displayName)
        }
    }

    private fun onLoginRedirectClicked(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun redirectToMenuActivity() {
        registerErrorTextView.text = ""

        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    override fun showErrorMessage(message: String) {
        when {
            message.startsWith("DisplayName is already") -> registerErrorTextView.text =
                registerErrors[8]
            message.startsWith("The email address is badly formatted") -> registerErrorTextView.text =
                registerErrors[9]
            message.startsWith("The email address is already in use") -> registerErrorTextView.text =
                registerErrors[10]
            else -> registerErrorTextView.text = message
        }
    }
}