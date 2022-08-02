package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.model.game.DifficultyLevel
import de.thm.mow2.fleetbattlegameandroid.model.game.Game
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService

class GameResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_result)

        backMenuButton = findViewById(R.id.backMenuButton)
        backMenuButton.setOnClickListener(::onBackMenuClicked)

        infoTextView = findViewById(R.id.infoText)
        winnerTextView = findViewById(R.id.winnerText)
        difficultyTextView = findViewById(R.id.difficultyText)
        winnerImg = findViewById(R.id.winnerImg)

        infoTexts = resources.getStringArray(R.array.info_text)
        winnerTexts = resources.getStringArray(R.array.winner_text)
        difficultyTexts = resources.getStringArray(R.array.difficulty_text)

        game = intent.getSerializableExtra("game") as Game

        mode = game.mode
        difficultyLevel = game.difficultyLevel
        winner = game.winner

        showInfoText()
    }

    private lateinit var backMenuButton: Button

    private lateinit var infoTextView: TextView
    private lateinit var winnerTextView: TextView
    private lateinit var difficultyTextView: TextView
    private lateinit var winnerImg: ImageView

    private lateinit var infoTexts: Array<String>
    private lateinit var winnerTexts: Array<String>
    private lateinit var difficultyTexts: Array<String>

    private lateinit var mode: GameMode
    private var difficultyLevel: DifficultyLevel? = null
    private var winner: String? = null
    private lateinit var game: Game

    private fun showInfoText() {
        val ownUsername = AuthenticationService.getUsername()!!
        val otherUsername = if (game.playerName1 == ownUsername) game.playerName2 else game.playerName1

        when (mode) {
            GameMode.SINGLEPLAYER -> {
                winner?.let { winnerName ->
                    if (winnerName == ownUsername) {
                        infoTextView.text = infoTexts[0]
                        winnerTextView.text = winnerTexts[3]
                        winnerImg.setImageResource(R.drawable.result_winner)
                    } else {
                        infoTextView.text = infoTexts[1]
                        winnerTextView.text = winnerTexts[4]
                        winnerImg.setImageResource(R.drawable.result_loser)
                    }
                    when (difficultyLevel) {
                        DifficultyLevel.EASY -> difficultyTextView.text = difficultyTexts[0]
                        DifficultyLevel.MEDIUM -> difficultyTextView.text = difficultyTexts[1]
                        DifficultyLevel.HARD -> difficultyTextView.text = difficultyTexts[2]
                    }
                }
                if (winner == null) {
                    infoTextView.text = infoTexts[2]
                    winnerTextView.text = winnerTexts[5]
                    winnerImg.setImageResource(R.drawable.result_quit)
                }
            }
            GameMode.MULTIPLAYER -> {
                winner?.let { winnerName ->
                    if (winnerName == ownUsername) {
                        infoTextView.text = infoTexts[0]
                        winnerTextView.text = HtmlCompat.fromHtml(
                            winnerTexts[0] + " <u>" + otherUsername + "</u> " + winnerTexts[1],
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        winnerImg.setImageResource(R.drawable.result_winner)
                    } else {
                        infoTextView.text = infoTexts[1]
                        winnerTextView.text = HtmlCompat.fromHtml(
                            winnerTexts[0] + " <u>" + otherUsername + "</u> " + winnerTexts[2],
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                        winnerImg.setImageResource(R.drawable.result_loser)
                    }
                }
                if (winner == null) {
                    infoTextView.text = infoTexts[2]
                    winnerTextView.text = winnerTexts[5]
                    winnerImg.setImageResource(R.drawable.result_quit)
                }
            }
        }
    }

    private fun onBackMenuClicked(view: View) {
        if (game.mode == GameMode.SINGLEPLAYER) {
            DatabaseService.deleteOldSingleplayerGameOfUser()
        } else {
            DatabaseService.deleteFinishedMultiplayerGamesOfUser()
        }
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

}
