package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textview.MaterialTextView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.helper.DateTimeHelper
import de.thm.mow2.fleetbattlegameandroid.controller.model.SinglePlayerGameSettingsController
import de.thm.mow2.fleetbattlegameandroid.controller.model.SingleplayerGameSettingsModel
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.game.DifficultyLevel
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import kotlinx.android.synthetic.main.activity_singleplayer_game_settings.*

class SingleplayerGameSettingsActivity : AppCompatActivity(), SinglePlayerGameSettingsController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singleplayer_game_settings)
        model = SingleplayerGameSettingsModel(this)
        difficultyToggleButton = findViewById(R.id.difficultyToggleButtonGroup)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_singleplayer_settings)

        difficultyToggleButton = findViewById(R.id.difficultyToggleButtonGroup)
        submitButton = findViewById(R.id.submitMenuButton)
        backButton = findViewById(R.id.action_bar_backButton)

        gameDetailsDescription = findViewById(R.id.gameDetailsDescription)
        gameDetailsDescription.background.alpha = 110
        gameDetailsSelection = GameService.getFleetBattleGameById("1")
        setGameDetailsDescriptionText()

        spinner = findViewById(R.id.progressBar)
        spinner.visibility = View.GONE

        difficultyToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (isChecked) {
                when (checkedId) {
                    R.id.easyToggleButton ->
                        difficultyLevel = DifficultyLevel.EASY
                    R.id.middleToggleButton ->
                        difficultyLevel = DifficultyLevel.MEDIUM
                    R.id.hardToggleButton ->
                        difficultyLevel = DifficultyLevel.HARD
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
            gameDetailsSelection = gameDetailsList[position]
            setGameDetailsDescriptionText()
        }
    }

    fun onClickSubmitButton(view: View) {
        val formattedDate = DateTimeHelper.getCurrentDateAsString()
        val gameMode: GameMode = GameMode.SINGLEPLAYER
        val fleetBattleGame = FleetBattleGame(
            gameDetailsSelection,
            formattedDate,
            gameMode,
            AuthenticationService.getUsername()!!,
            difficultyLevel
        )
        spinner.visibility = View.VISIBLE
        model.saveNewGame(fleetBattleGame)
    }

    fun onClickBackButton(view: View) {
        finish()
    }

    private lateinit var model: SingleplayerGameSettingsModel
    private lateinit var difficultyToggleButton: MaterialButtonToggleGroup
    private lateinit var submitButton: Button
    private lateinit var gameDetailsDescription: MaterialTextView
    private var difficultyLevel: DifficultyLevel = DifficultyLevel.EASY
    private lateinit var gameDetailsSelection: FleetBattleGameDetails
    private lateinit var backButton: Button
    private lateinit var spinner: ProgressBar

    private fun setGameDetailsDescriptionText() {
        var combinedText = "Maße: ${gameDetailsSelection.gridWidth}x${gameDetailsSelection.gridHeight}\n"
        for (shipDetail in gameDetailsSelection.shipDetailList) {
            val fieldValue = if (shipDetail.length != 1) "Felder" else "Feld"
            combinedText += "\n${shipDetail.numberOfThisType}x ${shipDetail.name} (${shipDetail.length} $fieldValue)"
        }

        gameDetailsDescription.text = combinedText
    }

    override fun creationSucceed(game: FleetBattleGame) {
        val intent = Intent(this, PrepareGameActivity::class.java)
        intent.putExtra("game", game)
        spinner.visibility = View.GONE
        startActivity(intent)
        finish()
    }
}