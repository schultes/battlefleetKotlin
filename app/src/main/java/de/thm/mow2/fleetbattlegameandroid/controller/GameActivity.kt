package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.textview.MaterialTextView
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.model.GameModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.GameController
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGrid
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.controller.adapter.AdvancedFieldAdapter
import de.thm.mow2.fleetbattlegameandroid.controller.helper.SnapshotListenerHelper
import java.util.stream.Collectors

class GameActivity : AppCompatActivity(), GameController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_game)

        val backButton: Button = findViewById(R.id.actionbar_game_cancel_button)
        backButton.setOnClickListener(::onClickGameBackButton)

        model = GameModel(this)
        username = AuthenticationService.getUsername()!!

        game = intent.getSerializableExtra("game") as FleetBattleGame
        snapshotListener = SnapshotListenerHelper()
        snapshotListener.startListening(game.documentId!!) { game, error ->
            model.snapShotListener(
                game,
                error,
                snapshotListener
            )
        }

        gameDetails = GameService.getFleetBattleGameById(game.gameDetailsId)

        vsTextView = findViewById(R.id.playerVsPlayer)

        turnTextView = findViewById(R.id.whoseTurn)

        gameBoard = findViewById(R.id.gameBoard)

        shipsTextView = findViewById(R.id.ships)
        val shipNames: List<String> = gameDetails.shipDetailList.map { element -> element.name }
        shipsTextView.text = shipNames.stream().collect(Collectors.joining("\n"))
        shipsTextView.text = "${getString(R.string.fleet)}:\n${shipsTextView.text}"
        shipsTextView.background.alpha = 100

        playerShips = arrayOf(findViewById(R.id.playerOne), findViewById(R.id.playerTwo))
        playerShips.forEach {
            it.background.alpha = 100
        }

        val currentUserTurn = if (game.usersTurn == 1) game.playerName1 else game.playerName2
        currentUserTurn?.let { currentUserTurnName ->
            gameGrid = if (username == currentUserTurnName) {
                model.getGridFromPlayer(game, username, true)
            } else {
                model.getGridFromPlayer(game, username)
            }
        } ?: run {
            gameGrid = model.getGridFromPlayer(game, username, true)
        }

        adapter = AdvancedFieldAdapter(this, gameGrid, null) { fleetField ->
            if (isGameBoardClickable) {
                val gameCopy = FleetBattleGame.toObject(FleetBattleGame.toMap(game))!! //game deepCopy
                gameCopy.documentId = game.documentId
                model.onFieldClicked(gameCopy, fleetField, username)
            }
        }
        adapter.setHasStableIds(true)
        (gameBoard.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        gameBoard.layoutManager = GridLayoutManager(this, gameDetails.gridWidth)
        gameBoard.adapter = adapter
    }

    override var isGameBoardClickable = true

    private lateinit var model: GameModel
    private lateinit var username: String

    private lateinit var game: FleetBattleGame
    private lateinit var gameDetails: FleetBattleGameDetails
    private lateinit var gameGrid: FleetBattleGrid

    private lateinit var vsTextView: MaterialTextView
    private lateinit var turnTextView: MaterialTextView

    private lateinit var gameBoard: RecyclerView
    private lateinit var adapter: AdvancedFieldAdapter

    private lateinit var shipsTextView: MaterialTextView
    private lateinit var playerShips: Array<MaterialTextView>

    private lateinit var snapshotListener: SnapshotListenerHelper

    private fun onClickGameBackButton(view: View) {
        snapshotListener.removeListener()
        val message =
            if (game.mode == GameMode.MULTIPLAYER) "Das Spiel selbst bleibt erhalten." else "Dein Spielstand geht dabei verloren."
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Willst du das Spiel wirklich verlassen? $message")
            .setPositiveButton("Ja") { _, _ ->
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            .setNegativeButton("Nein") { _, _ ->
                // Do nothing
            }
        builder.create().show()
    }

    override fun onGameUpdate(game: FleetBattleGame) {
        this.game = game

        val currentUserTurn = if (game.usersTurn == 1) game.playerName1 else game.playerName2

        // Get username by userTurn
        currentUserTurn?.let { currentUserTurnName ->
            gameGrid = if (username == currentUserTurnName) {
                model.getGridFromPlayer(game, username, true)
            } else {
                model.getGridFromPlayer(game, username)
            }
        } ?: run {
            gameGrid = model.getGridFromPlayer(game, username, true)
        }

        // Player names
        vsTextView.text = game.playerName1
        game.playerName2?.let { playerName2 ->
            vsTextView.text = "${vsTextView.text} ${getString(R.string.vs_big)} $playerName2"
        } ?: run {
            vsTextView.text =
                "${vsTextView.text} ${getString(R.string.vs_big)} ${getString(R.string.enemyMissing)}"
        }

        //Update gameboard
        adapter.updateGrid(gameGrid)

        // Update Status
        if (game.playerName2 == null) {
            turnTextView.text = getString(R.string.waiting_for_second_player)
            turnTextView.setBackgroundColor(Color.TRANSPARENT)
            turnTextView.setTextColor(getColor(R.color.primary_dark))
        } else if (game.gridFromPlayer1 == null || game.gridFromPlayer2 == null) {
            turnTextView.text = getString(R.string.waiting_for_all_grids)
            turnTextView.setBackgroundColor(Color.TRANSPARENT)
            turnTextView.setTextColor(getColor(R.color.primary_dark))
        } else {
            val text: String
            if (username == currentUserTurn) {
                text =
                    "${getString(R.string.your_are)} ${getString(R.string.whoseTurn)}"
                turnTextView.setBackgroundColor(getColor(R.color.primary_dark))
            } else {
                text =
                    "$currentUserTurn ${getString(R.string.user_is)} ${getString(R.string.whoseTurn)}"
                turnTextView.setBackgroundColor(getColor(R.color.secondary_dark))
            }
            turnTextView.setTextColor(getColor(R.color.white))
            turnTextView.text = text
        }

        // Calculate ship count of each player
        val shipCount: Array<Array<Int>> =
            arrayOf(Array(gameDetails.gridWidth + 1) { 0 }, Array(gameDetails.gridWidth + 1) { 0 })

        // Player 1
        playerShips[0].text = game.playerName1
        game.gridFromPlayer1?.let { gridObject ->
            for (ship in gridObject.ships.indices) {
                if (!gridObject.ships[ship].submerged) {
                    shipCount[0][gridObject.ships[ship].length]++
                }
            }
            for (ship in gameDetails.shipDetailList.indices) {
                playerShips[0].text =
                    "${playerShips[0].text}\n${shipCount[0][gameDetails.shipDetailList[ship].length]}"
            }
        } ?: run {
            // Player 1 has no grid yet (default values)
            for (ship in gameDetails.shipDetailList.indices) {
                playerShips[0].text =
                    "${playerShips[0].text}\n${gameDetails.shipDetailList[ship].numberOfThisType}"
            }
        }

        // Player 2
        game.gridFromPlayer2?.let { gridObject ->
            playerShips[1].text = game.playerName2
            for (ship in gridObject.ships.indices) {
                if (!gridObject.ships[ship].submerged) {
                    shipCount[1][gridObject.ships[ship].length]++
                }
            }
            for (ship in gameDetails.shipDetailList.indices) {
                playerShips[1].text =
                    "${playerShips[1].text}\n${shipCount[1][gameDetails.shipDetailList[ship].length]}"
            }
        } ?: run {
            // Player 2 has no grid yet (default values)
            playerShips[1].text =
                if (game.playerName2 != null) game.playerName2 else getString(R.string.enemyMissing)
            for (ship in gameDetails.shipDetailList.indices) {
                playerShips[1].text =
                    "${playerShips[1].text}\n${gameDetails.shipDetailList[ship].numberOfThisType}"
            }
        }

        if (game.isFinished) {
            snapshotListener.removeListener()
            val timer1 = object : CountDownTimer(900, 500) { // show gameResult
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    showGameResult(game)
                    return
                }
            }
            timer1.start()
        } else {
            isGameBoardClickable = true

            if (game.mode == GameMode.SINGLEPLAYER && game.usersTurn == 2 && model.gameKi != null) {
                val gameCopy = FleetBattleGame.toObject(FleetBattleGame.toMap(game))!! //game deepCopy
                gameCopy.documentId = game.documentId
                model.gameKi!!.updateGrid(gameCopy.gridFromPlayer1!!)
                val field = gameCopy.gridFromPlayer1!!.getField(model.gameKi!!.getNextTurn(gameCopy.lastMoveBy == game.playerName2))
                model.onFieldClicked(gameCopy, field, gameCopy.playerName2!!)
            }
        }
    }

    override fun showMove(fleetBattleGame: FleetBattleGame) {
        val oldGame = game

        val lastHit = fleetBattleGame.lastFiredPosition!!
        val gridToAttack = model.getGridFromPlayer(
            oldGame,
            username,
            fleetBattleGame.lastMoveBy == username
        )

        // Time before showing move (set more time for KI move)
        var durationBeforeShowingMove: Long = 100
        if (game.mode == GameMode.SINGLEPLAYER) {
            if (fleetBattleGame.lastMoveBy == game.playerName2 && game.lastMoveBy != game.playerName2) { // First move of ki
                durationBeforeShowingMove = 1500
            } else if (fleetBattleGame.lastMoveBy == game.playerName2) {
                durationBeforeShowingMove = 900
            }
        }

        val timer1 = object : CountDownTimer(durationBeforeShowingMove, durationBeforeShowingMove) { // old grid
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                val text: String
                val currentUserTurn = if (oldGame.usersTurn == 1) oldGame.playerName1 else oldGame.playerName2
                if (username == currentUserTurn) {
                    text = "Feuer!"
                    turnTextView.setBackgroundColor(getColor(R.color.primary_dark))
                } else {
                    text = "Gegner feuert!"
                    turnTextView.setBackgroundColor(getColor(R.color.secondary_dark))
                }
                turnTextView.setTextColor(getColor(R.color.white))
                turnTextView.text = text
                adapter.showMove(lastHit)

                val timer2 = object : CountDownTimer(1500, 1500) { // Duration for showing crosshair
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {

                        // Stop Showing crosshair and show field value
                        gridToAttack.fields[lastHit.row][lastHit.column].isVisible = true
                        adapter.showMove(null)
                        adapter.updateGrid(gridToAttack)
                        val showResultDuration: Long = if (fleetBattleGame.usersTurn == oldGame.usersTurn) 1000 else 1500

                        val timer3 = object : CountDownTimer(showResultDuration, showResultDuration) { // Duration for showing result
                            override fun onTick(millisUntilFinished: Long) {}

                            override fun onFinish() { // prepare view for next move
                                onGameUpdate(fleetBattleGame)
                            }
                        }
                        timer3.start()
                    }
                }
                timer2.start()
            }
        }
        timer1.start()
    }

    override fun showGameResult(game: FleetBattleGame) {
        snapshotListener.removeListener()
        val intent = Intent(this, GameResultActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }

}