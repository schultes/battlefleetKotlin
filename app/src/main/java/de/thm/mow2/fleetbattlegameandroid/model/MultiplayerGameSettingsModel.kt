package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService
import de.thm.mow2.fleetbattlegameandroid.model.game.User

interface MultiplayerGameSettingsController {
    fun setOtherUsernames(list: List<User>)
    fun updateSucceed(game: FleetBattleGame)
}

class MultiplayerGameSettingsModel(private val controller: MultiplayerGameSettingsController) {

    fun saveNewGame(game: FleetBattleGame) {
        DatabaseService.saveFleetBattleGameObject(FleetBattleGame.toMap(game)) { result, error ->
            result?.let { savedGame ->
                controller.updateSucceed(savedGame)
            }
            error?.let { message ->
                println(message)
            }
        }
    }

    fun getOtherUsernames() {
        DatabaseService.getOtherUsernames() { result, error ->
            result?.let { list ->
                controller.setOtherUsernames(list)
            }
            error?.let { message ->
                println(message)
            }
        }
    }
}