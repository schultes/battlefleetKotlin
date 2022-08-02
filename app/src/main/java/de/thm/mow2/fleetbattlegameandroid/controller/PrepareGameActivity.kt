package de.thm.mow2.fleetbattlegameandroid.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import de.thm.mow2.fleetbattlegameandroid.R
import de.thm.mow2.fleetbattlegameandroid.controller.adapter.FieldAdapter
import de.thm.mow2.fleetbattlegameandroid.controller.model.PrepareGameModel
import de.thm.mow2.fleetbattlegameandroid.controller.model.PrepareGameController
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.helper.GridHelperObject
import de.thm.mow2.fleetbattlegameandroid.model.helper.PlaceShipFeedback
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.model.ship.Ship
import java.util.stream.Collectors

class PrepareGameActivity : AppCompatActivity(), PrepareGameController {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preparegame)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_prepare_game)
        checkBtn = findViewById(R.id.CheckMyPlacedShipsButton)

        val backButton: Button = findViewById(R.id.actionbar_prepare_cancel_button)
        val generateRandomShipsButton: Button =
            findViewById(R.id.action_bar_place_random_ships)

        backButton.setOnClickListener(::onClickPrepareGamesBackButton)
        generateRandomShipsButton.setOnClickListener(::generateRandomGrid)

        game = intent.getSerializableExtra("game") as FleetBattleGame

        model = PrepareGameModel(this)
        gameDetails = GameService.getFleetBattleGameById(game.gameDetailsId)
        width = gameDetails.gridWidth
        height = gameDetails.gridHeight

        cells = model.initGridHelperObjects(width, height)

        errorMessageTextView = findViewById(R.id.errorMessage)
        battleShipsTextView = findViewById(R.id.howManyBattleShipsText)
        showFeedbackAfterClick(PlaceShipFeedback(mutableMapOf(), mutableMapOf(), ""))
        battleShipsTextView.background.alpha = 100

        recyclerView = findViewById(R.id.recyclerview)
        adapter = FieldAdapter(this) { gridHelper ->
            gridHelper.isShipSet = gridHelper.isShipSet.not()
            adapter.updateList(cells)
            model.checkGridValidation(gameDetails, cells, false)
        }
        adapter.setHasStableIds(true)
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        recyclerView.layoutManager = GridLayoutManager(this, width)
        recyclerView.adapter = adapter

        recyclerView.post {
            adapter.cells.clear()
            adapter.cells.addAll(cells)
            adapter.notifyDataSetChanged()
        }

        val battleShipsTextView: TextView = findViewById(R.id.howManyBattleShipsText)
        val descriptions: List<String> =
            gameDetails.shipDetailList.map { element -> "${element.numberOfThisType}x ${element.name} (${element.length} ${if (element.length != 1) "Felder" else "Feld"})" }
        battleShipsTextView.text = descriptions.stream().collect(Collectors.joining("\n"))
        battleShipsTextView.background.alpha = 100
    }

    fun checkMyPlacedShips(view: android.view.View) {
        model.checkGridValidation(gameDetails, cells, true)
    }

    private lateinit var model: PrepareGameModel

    private var width = 0
    private var height = 0
    private lateinit var cells: ArrayList<GridHelperObject>
    private lateinit var gameDetails: FleetBattleGameDetails
    private lateinit var game: FleetBattleGame

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FieldAdapter
    private lateinit var errorMessageTextView: TextView
    private lateinit var battleShipsTextView: TextView
    private lateinit var checkBtn: Button

    private fun generateRandomGrid(view: View) {
        cells = model.getRandomPlacedShips(gameDetails)
        recyclerView.post {
            adapter.cells.clear()
            adapter.cells.addAll(cells)
            adapter.notifyDataSetChanged()
        }
        model.checkGridValidation(gameDetails, cells, false)
    }

    override fun showFeedbackAfterClick(feedback: PlaceShipFeedback) {
        var allShipsAreSet = true
        val descriptions: List<String> = gameDetails.shipDetailList.map { element ->
            val fieldName = if (element.length != 1) "Felder" else "Feld"
            feedback.currentShipMap[element.length]?.let { value ->
                when {
                    value == element.numberOfThisType -> {
                        "<font color=${
                            "#" + Integer.toHexString(getColor(R.color.green)).substring(2)
                        }>${element.numberOfThisType}x ${element.name} (${element.length} $fieldName)&nbsp;</font>"
                    }
                    value > element.numberOfThisType -> {
                        allShipsAreSet = false
                        "<font color=${
                            "#" + Integer.toHexString(getColor(R.color.secondary_normal))
                                .substring(2)
                        }>${element.numberOfThisType}x ${element.name} (${element.length} $fieldName)&nbsp;</font>"
                    }
                    else -> {
                        allShipsAreSet = false
                        "<font color=${
                            "#" + Integer.toHexString(getColor(R.color.primary_dark))
                                .substring(2)
                        }>${element.numberOfThisType}x ${element.name} (${element.length} $fieldName)&nbsp;</font>"
                    }
                }
            } ?: run {
                allShipsAreSet = false
                "<font color=${
                    "#" + Integer.toHexString(getColor(R.color.primary_dark)).substring(2)
                }>${element.numberOfThisType}x ${element.name} (${element.length} $fieldName)&nbsp;</font>"
            }

        }
        if (allShipsAreSet && feedback.gameConfig == feedback.currentShipMap) {
            checkBtn.setBackgroundColor(getColor(R.color.secondary_normal))
        } else {
            checkBtn.setBackgroundColor(getColor(R.color.grey))
        }

        val connectedString = descriptions.stream().collect(Collectors.joining("<br />"))
        battleShipsTextView.text =
            HtmlCompat.fromHtml(connectedString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        errorMessageTextView.text =
            if (allShipsAreSet && feedback.gameConfig != feedback.currentShipMap) "Ungültiger Schifftyp gefunden" else feedback.errorMessage
    }

    override fun showErrorMessage(message: String) {
        errorMessageTextView.text = message
    }

    override fun startGameActivity(game: FleetBattleGame) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }

    override fun startGameResultActivity(game: FleetBattleGame) {
        val intent = Intent(this, GameResultActivity::class.java)
        intent.putExtra("game", game)
        startActivity(intent)
        finish()
    }

    override fun setShipList(shipList: List<Ship>) {
        model.saveGridInGame(shipList, gameDetails, game)
    }

    private fun onClickPrepareGamesBackButton(view: View) {
        val message =
            if (game.mode == GameMode.MULTIPLAYER) "Das Spiel selbst bleibt erhalten." else "Dein Spielstand geht dabei verloren."
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Willst du die Platzierung abbrechen? $message")
            .setPositiveButton("Ja") { _, _ ->
                finish()
            }
            .setNegativeButton("Nein") { _, _ ->
                // Do nothing
            }
        builder.create().show()
    }
}