package de.thm.mow2.fleetbattlegameandroid.model.core

import de.thm.mow2.fleetbattlegameandroid.model.game.DifficultyLevel
import de.thm.mow2.fleetbattlegameandroid.model.game.Game
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.tp.library.random.TPRandom
import java.io.Serializable

class FleetBattleGame(
    fleetBattleGameDetails: FleetBattleGameDetails,
    override var createdAt: String,
    override var mode: GameMode,
    override var playerName1: String,
    override var difficultyLevel: DifficultyLevel?,
    override var gridHeight: Int = 0,
    override var gridWidth: Int = 0,
    override var playerName2: String? = null,
    override var isFinished: Boolean = false,
    override var winner: String? = null,
    override var usersTurn: Int = 1,
    override var lastMoveBy: String? = null
) : Game(), Serializable {
    var documentId: String? = null
    val gameDetailsId: String
    var gridFromPlayer1: FleetBattleGrid? = null
    var gridFromPlayer2: FleetBattleGrid? = null
    var lastFiredPosition: Position? = null

    constructor(
        fleetBattleGameDetails: FleetBattleGameDetails,
        createdAt: String,
        gridHeight: Int,
        gridWidth: Int,
        mode: GameMode,
        playerName1: String,
        playerName2: String?,
        isFinished: Boolean,
        winner: String?,
        usersTurn: Int,
        difficultyLevel: DifficultyLevel?,
        lastMoveBy: String?,
        gridFromPlayer1: FleetBattleGrid?,
        gridFromPlayer2: FleetBattleGrid?,
        lastFiredPosition: Position?
    ) : this(fleetBattleGameDetails, createdAt, mode, playerName1, difficultyLevel) {
        this.createdAt = createdAt
        this.gridHeight = gridHeight
        this.gridWidth = gridWidth
        this.mode = mode
        this.playerName1 = playerName1
        this.playerName2 = playerName2
        this.isFinished = isFinished
        this.winner = winner
        this.usersTurn = usersTurn
        this.difficultyLevel = difficultyLevel
        this.lastMoveBy = lastMoveBy
        this.gridFromPlayer1 = gridFromPlayer1
        this.gridFromPlayer2 = gridFromPlayer2
        this.lastFiredPosition = lastFiredPosition
    }

    init {
        gameDetailsId = fleetBattleGameDetails.id
        gridHeight = fleetBattleGameDetails.gridHeight
        gridWidth = fleetBattleGameDetails.gridWidth
        usersTurn = TPRandom.int(1, 3)
    }

    companion object {
        const val COLLECTION_NAME: String = "fleetBattleGames"

        fun toObject(map: Map<String, Any>): FleetBattleGame? {
            val playername2Object =
                if (map["playerName2"] != "") map["playerName2"]!! else null
            val winnerObject =
                if (map["winner"] != "") map["winner"]!! else null
            val difficultyLevelObject =
                if (map["difficultyLevel"] != "") DifficultyLevel(map["difficultyLevel"]!! as String)!! else null
            val gridFromPlayer1Object =
                if (map["gridFromPlayer1"] != "") FleetBattleGrid.toObject(
                    map["gridFromPlayer1"] as Map<String, Any>,
                    map["gameDetailsId"] as String
                ) else null
            val gridFromPlayer2Object =
                if (map["gridFromPlayer2"] != "") FleetBattleGrid.toObject(
                    map["gridFromPlayer2"] as Map<String, Any>,
                    map["gameDetailsId"] as String
                ) else null
            val lastFiredPositionObject =
                if (map["lastFiredPosition"] != "") Position.toObject(map["lastFiredPosition"] as Map<String, Any>) else null
            val lastMoveByObject =
                if (map["lastMoveBy"] != "") map["lastMoveBy"]!! as String? else null
            return if (
                map["createdAt"] == null
                || map["gridHeight"] == null
                || map["gridWidth"] == null
                || map["mode"] == null
                || map["playerName1"] == null
                || map["isFinished"] == null
            ) null
            else FleetBattleGame(
                GameService.getFleetBattleGameById(map["gameDetailsId"] as String),
                map["createdAt"] as String,
                (map["gridHeight"] as Number).toInt(),
                (map["gridWidth"] as Number).toInt(),
                GameMode(map["mode"]!! as String)!!,
                map["playerName1"] as String,
                playername2Object as String?,
                map["isFinished"] as Boolean,
                winnerObject as String?,
                (map["usersTurn"] as Number).toInt(),
                difficultyLevelObject,
                lastMoveByObject,
                gridFromPlayer1Object,
                gridFromPlayer2Object,
                lastFiredPositionObject
            )
        }

        fun toMap(fleetBattleGame: FleetBattleGame): Map<String, Any> {
            val playernameObject =
                if (fleetBattleGame.playerName2 != null) fleetBattleGame.playerName2!! else ""
            val winnerObject =
                if (fleetBattleGame.winner != null) fleetBattleGame.winner!! else ""
            val lastMoveByObject =
                if (fleetBattleGame.lastMoveBy != null) fleetBattleGame.lastMoveBy!! else ""
            val difficultyLevelObject =
                if (fleetBattleGame.difficultyLevel != null) fleetBattleGame.difficultyLevel!!.rawValue else ""
            val gridFromPlayer1Object =
                if (fleetBattleGame.gridFromPlayer1 != null) FleetBattleGrid.toMap(fleetBattleGame.gridFromPlayer1!!) else ""
            val gridFromPlayer2Object =
                if (fleetBattleGame.gridFromPlayer2 != null) FleetBattleGrid.toMap(fleetBattleGame.gridFromPlayer2!!) else ""
            val lastFiredPositionObject =
                if (fleetBattleGame.lastFiredPosition != null) Position.toMap(fleetBattleGame.lastFiredPosition!!) else ""

            return mapOf(
                "createdAt" to fleetBattleGame.createdAt,
                "gridHeight" to fleetBattleGame.gridHeight,
                "gridWidth" to fleetBattleGame.gridWidth,
                "mode" to fleetBattleGame.mode.rawValue,
                "playerName1" to fleetBattleGame.playerName1,
                "playerName2" to playernameObject,
                "playerNames" to listOf(fleetBattleGame.playerName1, playernameObject),
                "isFinished" to fleetBattleGame.isFinished,
                "winner" to winnerObject,
                "usersTurn" to fleetBattleGame.usersTurn,
                "difficultyLevel" to difficultyLevelObject,
                "lastMoveBy" to lastMoveByObject,
                "gameDetailsId" to fleetBattleGame.gameDetailsId,
                "gridFromPlayer1" to gridFromPlayer1Object,
                "gridFromPlayer2" to gridFromPlayer2Object,
                "lastFiredPosition" to lastFiredPositionObject
            )
        }
    }
}