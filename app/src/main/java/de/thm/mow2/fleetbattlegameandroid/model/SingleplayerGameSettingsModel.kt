package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService

interface SinglePlayerGameSettingsController {
    fun creationSucceed(game: FleetBattleGame)
}

class SingleplayerGameSettingsModel(private val controller: SinglePlayerGameSettingsController) {

    fun saveNewGame(game: FleetBattleGame) {
        DatabaseService.saveFleetBattleGameObject(FleetBattleGame.toMap(game)) { result, error ->
            result?.let { savedGame ->
                controller.creationSucceed(savedGame)
            }
            error?.let { message ->
                println(message)
            }
        }
    }
}