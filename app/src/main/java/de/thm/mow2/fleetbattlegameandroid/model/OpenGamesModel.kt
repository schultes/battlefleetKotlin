package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService

interface OpenGamesController {
    fun setRunningGamesOfUser(list: List<FleetBattleGame>)
    fun setOpenGameRequestsOfUser(list: List<FleetBattleGame>)
    fun setOtherOpenGameRequestsOfUser(list: List<FleetBattleGame>)
    fun updateSucceed(game: FleetBattleGame)
    fun startGameResultActivity(game: FleetBattleGame)
}

class OpenGamesModel(val controller: OpenGamesController) {

    fun updatePlayer2(game: FleetBattleGame) {
        game.playerName2 = AuthenticationService.getUsername()!!
        val changedAttributes = mapOf("playerName2" to game.playerName2!!, "playerNames" to listOf(game.playerName1, game.playerName2!!))
        DatabaseService.updateFleetBattleGameObject(game.documentId!!, changedAttributes) { errorMessage ->
            if (errorMessage == null) {
                controller.updateSucceed(game)
            } else {
                controller.startGameResultActivity(game)
            }
        }
    }

    fun loadRunningGamesOfUser() {
        DatabaseService.getAllRunningGamesFromUser() { result, error ->
            result?.let { list ->
                controller.setRunningGamesOfUser(list)
            }
            error?.let { message ->
                println(message)
            }
        }
    }

    fun loadOpenGameRequestsOfUser() {
        DatabaseService.getAllOpenGameRequestsFromUser() { result, error ->
            result?.let { list ->
                controller.setOpenGameRequestsOfUser(list)
            }
            error?.let { message ->
                println(message)
            }
        }
    }

    fun loadOtherOpenGameRequests() {
        DatabaseService.loadAllOpenFleetBattleGames() { result, error ->
            result?.let { list ->
                controller.setOtherOpenGameRequestsOfUser(list)
            }
            error?.let { message ->
                println(message)
            }
        }
    }
}