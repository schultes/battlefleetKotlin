package de.thm.mow2.fleetbattlegameandroid.model.core

import de.thm.mow2.fleetbattlegameandroid.model.grid.Field
import de.thm.mow2.fleetbattlegameandroid.model.grid.Grid
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.model.ship.Ship
import java.io.Serializable

class FleetBattleGrid(
    fleetBattleGameDetails: FleetBattleGameDetails,
    override var width: Int = 0,
    override var height: Int = 0,
    override var fields: List<List<Field>> = ArrayList<ArrayList<Field>>()
) : Grid(), Serializable {
    var ownerUsername: String
    lateinit var ships: List<Ship>

    constructor(
        fleetBattleGameDetails: FleetBattleGameDetails,
        width: Int,
        height: Int,
        fields: List<List<Field>>,
        ownerUsername: String,
        ships: List<Ship>
    ) : this(fleetBattleGameDetails) {
        this.width = width
        this.height = height
        this.fields = fields
        this.ownerUsername = ownerUsername
        this.ships = ships
    }

    init {
        this.width = fleetBattleGameDetails.gridWidth
        this.height = fleetBattleGameDetails.gridHeight
        ownerUsername = fleetBattleGameDetails.name
    }


    companion object {
        fun toMap(fleetBattleGrid: FleetBattleGrid): Map<String, Any> {
            return mapOf(
                "width" to fleetBattleGrid.width,
                "height" to fleetBattleGrid.height,
                "fields" to FleetBattleField.multipleToMap(fleetBattleGrid.fields),
                "ownerUsername" to fleetBattleGrid.ownerUsername,
                "ships" to Ship.multipleToMap(fleetBattleGrid.ships)
            )
        }

        fun toObject(map: Map<String, Any>, gameDetailsId: String): FleetBattleGrid? {
            val width = (map["width"] as Number).toInt()
            val height = (map["height"] as Number).toInt()
            val ships = Ship.multipleToObject(map["ships"] as List<Map<String, Any>>)
            return FleetBattleGrid(
                GameService.getFleetBattleGameById(gameDetailsId),
                width,
                height,
                FleetBattleField.multipleToObject(
                    map["fields"] as List<Map<String, Any>>,
                    width,
                    height,
                    ships
                ),
                map["ownerUsername"] as String,
                ships
            )
        }
    }

}

