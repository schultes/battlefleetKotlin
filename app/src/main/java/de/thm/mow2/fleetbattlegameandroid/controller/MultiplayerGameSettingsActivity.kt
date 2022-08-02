package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textview.MaterialTextView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.helper.DateTimeHelper
import de.thm.mow2.fleetbattlegameandroid.controller.model.MultiplayerGameSettingsModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.MultiplayerGameSettingsController
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.game.User
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import kotlinx.android.synthetic.main.activity_multiplayer_game_settings.*

class MultiplayerGameSettingsActivity : AppCompatActivity(), MultiplayerGameSettingsController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game_settings)
        model = MultiplayerGameSettingsModel(this)
        decisionToggleButton = findViewById(R.id.decisionToggleButtonGroup)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_multiplayer_settings)

        decisionToggleButton = findViewById(R.id.decisionToggleButtonGroup)
        multiplayersubmitButton = findViewById(R.id.multiplayerSubmitMenuButton)
        backButton = findViewById(R.id.action_bar_multiplayer_backButton)

        gameMultiplayerDetailsDescription = findViewById(R.id.gameMutliplayerDetailsDescription)
        gameMultiplayerDetailsDescription.background.alpha = 110
        gameMultiplayerDetailsSelection = GameService.getFleetBattleGameById("1")
        setGameDetailsDescriptionText()

        spinner = findViewById(R.id.progressBar)
        spinner.visibility = View.GONE

        playerNames = listOf()
        model.getOtherUsernames()
        userAutoCompleteTextView = findViewById(R.id.username)
        usernameAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, playerNames)
        userAutoCompleteTextView.setAdapter(usernameAdapter)

        decisionToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (isChecked) {
                when (checkedId) {
                    R.id.noToggleButton -> {
                        isGameReserved = false
                        outlinedMultiplayerTextField.visibility = View.INVISIBLE
                    }
                    R.id.yesToggleButton -> {
                        isGameReserved = true
                        outlinedMultiplayerTextField.visibility = View.VISIBLE
                    }
                    else -> Toast.makeText(
                        this,
                        "Es darf nicht nichts ausgewählt sein",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Create array with all game details
        val gameDetailsList = GameService.listOfAllGameModes
        val playerGameModes = Array(gameDetailsList.size) { i -> gameDetailsList[i].name }

        // Create ArrayAdapter for dropdown menu
        val arrayAdapterForDropdown = ArrayAdapter(this, R.layout.drop_down_item_player_gamemode, playerGameModes)
        dropDownMenuAutoCompleteTextView.setAdapter(arrayAdapterForDropdown)
        dropDownMenuAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            gameMultiplayerDetailsSelection = gameDetailsList[position]
            setGameDetailsDescriptionText()
        }
    }

    fun onClickMultiplayerSubmitButton(view: View) {
        val formattedDate = DateTimeHelper.getCurrentDateAsString()
        val gameMode: GameMode = GameMode.MULTIPLAYER
        var playerName2: String? = null
        var isValid = false

        if (isGameReserved) {
            playerName2 = outlinedMultiplayerTextField.editText?.text.toString()
            (playerNames.find { name -> name == playerName2 })?.let {
                isValid = true
            } ?: run {
                Toast.makeText(
                    this,
                    "Kein gültiger Spielername",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            isValid = true
        }
        if (isValid) {
            val fleetBattleGame = FleetBattleGame(
                gameMultiplayerDetailsSelection,
                formattedDate,
                gameMode,
                AuthenticationService.getUsername()!!,
                null,
                0,
                0,
                playerName2
            )
            spinner.visibility = View.VISIBLE
            model.saveNewGame(fleetBattleGame)
        }
    }

    fun onClickBackButton(view: View) {
        val intent = Intent(this, OpenGamesActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private lateinit var model: MultiplayerGameSettingsModel
    private lateinit var decisionToggleButton: MaterialButtonToggleGroup
    private lateinit var multiplayersubmitButton: Button
    private lateinit var gameMultiplayerDetailsDescription: MaterialTextView
    private lateinit var gameMultiplayerDetailsSelection: FleetBattleGameDetails
    private lateinit var backButton: Button
    private var isGameReserved = false
    private lateinit var spinner: ProgressBar
    private lateinit var playerNames: List<String>
    private lateinit var userAutoCompleteTextView: AutoCompleteTextView
    private lateinit var usernameAdapter: ArrayAdapter<String>

    private fun setGameDetailsDescriptionText() {
        var combinedText =
            "Maße: ${gameMultiplayerDetailsSelection.gridWidth}x${gameMultiplayerDetailsSelection.gridHeight}\n"
        for (shipDetail in gameMultiplayerDetailsSelection.shipDetailList) {
            val fieldValue = if (shipDetail.length != 1) "Felder" else "Feld"
            combinedText += "\n${shipDetail.numberOfThisType}x ${shipDetail.name} (${shipDetail.length} $fieldValue)"
        }

        gameMultiplayerDetailsDescription.text = combinedText
    }

    override fun setOtherUsernames(list: List<User>) {
        playerNames = list.map { user -> user.username }.sorted()
        usernameAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, playerNames)
        userAutoCompleteTextView.setAdapter(usernameAdapter)
    }

    override fun updateSucceed(game: FleetBattleGame) {
        spinner.visibility = View.GONE
        redirectToOtherActivity(game)
    }

    private fun redirectToOtherActivity(game: FleetBattleGame) {
        val intent = Intent(this, PrepareGameActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }
}