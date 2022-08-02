package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService

class MenuActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var singlePlayerButton: Button
    private lateinit var multiPlayerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_default)

        logoutButton = findViewById(R.id.logoutButton)
        singlePlayerButton = findViewById(R.id.singlePlayerButton)
        multiPlayerButton = findViewById(R.id.multiPlayerButton)

    }

    fun onClickLogoutButton(view: View) {

        val builder = AlertDialog.Builder(this)

        builder.setMessage("Möchtest du dich wirklich ausloggen?")
            .setPositiveButton("Ja") { _, _ ->
                AuthenticationService.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Nein") { _, _ ->
                // Do nothing
            }

        builder.create().show()

    }

    fun onClickSinglePlayerButton(view: View) {
        val intent = Intent(this, SingleplayerGameSettingsActivity::class.java)
        startActivity(intent)
    }

    fun onClickMultiPlayerButton(view: View) {
        val intent = Intent(this, OpenGamesActivity::class.java)
        startActivity(intent)
    }

}