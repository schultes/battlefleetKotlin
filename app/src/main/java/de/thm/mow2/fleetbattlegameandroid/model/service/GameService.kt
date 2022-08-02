package de.thm.mow2.fleetbattlegameandroid.model.service

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.game.Game
import de.thm.mow2.fleetbattlegameandroid.model.ship.ShipDetails

class GameService {

    companion object {

        private val listOfShipsClassic = listOf(
            ShipDetails("Schlachtschiff", 5, 1),
            ShipDetails("Kreuzer", 4, 2),
            ShipDetails("Zerstörer", 3, 3),
            ShipDetails("U-Boot", 2, 4)
        )

        private val listOfShipsQuick = listOf(
            ShipDetails("Schlachtschiff", 5, 2),
            ShipDetails("Kreuzer", 4, 1),
            ShipDetails("Zerstörer", 3, 1)
        )

        private val listOfShipsSmall = listOf(
            ShipDetails("Kreuzer", 3, 1),
            ShipDetails("U-Boot", 2, 2),
            ShipDetails("Minensucher", 1, 1)
        )

        private val classic = FleetBattleGameDetails("1", "Klassik", 10, 10, listOfShipsClassic)
        private val quick = FleetBattleGameDetails("2", "Schnell", 7, 7, listOfShipsQuick)
        private val small = FleetBattleGameDetails("3", "Klein", 5, 5, listOfShipsSmall)

        val listOfAllGameModes = listOf(classic, quick, small)

        fun getFleetBattleGameById(id: String): FleetBattleGameDetails {
            val details = listOfAllGameModes.find { it.id == id }
            details?.let {
                return it
            }
            return classic
        }


        fun isUserPlayer1(game: Game): Boolean {
            AuthenticationService.getUsername()?.let { username ->
                return game.playerName1 == username
            }

            return false

        }

        fun isOwnGridSet(game: FleetBattleGame): Boolean {
            val currentPlayer = isUserPlayer1(game)

            if (currentPlayer) game.gridFromPlayer1?.let { return true }
            else game.gridFromPlayer2?.let { return true }

            return false
        }

    }

}