package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleField
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGrid
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleKI
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.grid.Field
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.model.service.SnapshotListenerInterface

interface GameController {
    var isGameBoardClickable: Boolean
    fun onGameUpdate(game: FleetBattleGame)
    fun showMove(fleetBattleGame: FleetBattleGame)
    fun showGameResult(game: FleetBattleGame)
}

class GameModel(private val controller: GameController) {

    private var currentGame: FleetBattleGame? = null
    var gameKi: FleetBattleKI? = null

    fun getGridFromPlayer(
        game: FleetBattleGame,
        player: String,
        other: Boolean = false
    ): FleetBattleGrid {
        var grid: FleetBattleGrid?
        if (other) {
            grid = if (player == game.playerName1) {
                game.gridFromPlayer2
            } else {
                game.gridFromPlayer1
            }
        } else {
            grid = if (player == game.playerName1) {
                game.gridFromPlayer1
            } else {
                game.gridFromPlayer2
            }
        }
        // Set empty grid to show a grid with the needed size, even if it is not ready yet
        if (grid == null) {
            grid = FleetBattleGrid(
                GameService.getFleetBattleGameById(game.gameDetailsId),
                game.gridWidth,
                game.gridHeight
            )
            val fields = ArrayList<ArrayList<Field>>()
            for (row in 0 until game.gridHeight) {
                val tempList = arrayListOf<Field>()
                for (column in 0 until game.gridWidth) {
                    tempList.add(FleetBattleField(null, Position(row, column), false))
                }
                fields += tempList
            }
            grid.fields = fields
        }
        return grid
    }

    fun onFieldClicked(game: FleetBattleGame, fleetField: Field, username: String) {
        if (game.gridFromPlayer1 == null || game.gridFromPlayer2 == null) {
            return
        }

        val currentUserTurn = if (game.usersTurn == 1) game.playerName1 else game.playerName2
        val enemyGrid = getGridFromPlayer(game, username, true)
        val hitField = enemyGrid.getField(fleetField.position)
        if (username == currentUserTurn && hitField.isVisible.not() && hitField is FleetBattleField) {
            controller.isGameBoardClickable = false
            hitField.isVisible = true
            game.lastFiredPosition = fleetField.position
            game.lastMoveBy = username

            hitField.ship?.let { ship ->
                val shipPositions = ship.getAllPositionsOfShip()
                var sub = true
                for (pos in shipPositions) {
                    if (enemyGrid.getField(pos).isVisible.not()) {
                        sub = false
                    }
                }
                ship.submerged = sub

                var finished = true
                for (enemyShip in enemyGrid.ships) {
                    if (enemyShip.submerged.not()) {
                        finished = false
                    }
                }
                game.isFinished = finished

                if (finished) {
                    game.winner = username
                }
            } ?: run {
                game.usersTurn = if (game.usersTurn == 1) 2 else 1
            }

            val gameMap = FleetBattleGame.toMap(game)

            DatabaseService.updateFleetBattleGameObject(game.documentId!!, gameMap) { error ->
                error?.let {
                    controller.showGameResult(game)
                }
            }
        }
    }

    fun snapShotListener(
        game: FleetBattleGame?,
        error: String?,
        listener: SnapshotListenerInterface
    ) {
        game?.let { receivedGame ->

            if (currentGame == null) { //Coming back to running game
                currentGame = receivedGame
                if (game.mode == GameMode.SINGLEPLAYER) {
                    gameKi = FleetBattleKI(game.difficultyLevel!!, game.gridFromPlayer1!!)
                }
                controller.onGameUpdate(receivedGame)
                return
            }

            if (receivedGame.isFinished && receivedGame.winner == null) { //Game canceled
                listener.removeListener()
                controller.showGameResult(receivedGame)
                return
            }

            if (receivedGame.playerName2 != null && receivedGame.gridFromPlayer1 != null && receivedGame.gridFromPlayer2 != null && receivedGame.lastMoveBy != null && receivedGame.lastFiredPosition != null) {
                controller.showMove(receivedGame)
            } else {
                controller.onGameUpdate(receivedGame)
            }
            currentGame = receivedGame
        }
        error?.let { // Game is canceled - go to result activity
            listener.removeListener()
            controller.showGameResult(
                game ?: FleetBattleGame(
                    GameService.getFleetBattleGameById("1"),
                    "",
                    GameMode.MULTIPLAYER,
                    "",
                    null
                )
            )
        }
    }

}