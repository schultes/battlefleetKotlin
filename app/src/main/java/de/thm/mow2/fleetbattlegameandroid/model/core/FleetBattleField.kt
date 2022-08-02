package de.thm.mow2.fleetbattlegameandroid.model.core

import de.thm.mow2.fleetbattlegameandroid.model.grid.Field
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.ship.Ship
import java.io.Serializable

data class FleetBattleField(
    val ship: Ship?,
    override var position: Position,
    override var isVisible: Boolean
) : Field(), Serializable {

    companion object {
        fun multipleToMap(fleetBattleFields: List<List<Field>>): List<Map<String, Any>> {
            val fleetBattleFieldMap = arrayListOf<Map<String, Any>>()

            fleetBattleFields.forEach { fleetBattleFieldRow ->
                fleetBattleFieldRow.forEach { fleetBattleFieldColumn ->
                    fleetBattleFieldMap.add(toMap(fleetBattleFieldColumn as FleetBattleField))
                }
            }
            return fleetBattleFieldMap
        }

        fun toMap(fleetBattleField: FleetBattleField): Map<String, Any> {
            val shipObject =
                if (fleetBattleField.ship != null) Ship.toMap(fleetBattleField.ship) else ""
            return mapOf(
                "position" to Position.toMap(fleetBattleField.position),
                "isVisible" to fleetBattleField.isVisible,
                "ship" to shipObject
            )
        }

        fun multipleToObject(map: List<Map<String, Any>>, columnCount: Int, rowCount: Int, shipList: List<Ship>): List<List<Field>> {
            val fleetBattleFields2D = arrayListOf<List<Field>>()
            val fleetBattleFields = arrayListOf<Field>()
            for (element in map) {
                fleetBattleFields.add(toObject(element, shipList)!!)
            }

            for (row in 0 until rowCount) {
                val tempList = arrayListOf<Field>()
                for (column in 0 until columnCount) {
                    tempList.add(fleetBattleFields[row * columnCount + column])
                }
                fleetBattleFields2D.add(tempList)
            }
            return fleetBattleFields2D
        }

        fun toObject(map: Map<String, Any>, shipList: List<Ship>): FleetBattleField? {
            var ship = if (map["ship"] != "") Ship.toObject(map["ship"] as Map<String, Any>) else null
            val position = Position.toObject(map["position"] as Map<String, Any>)!!
            ship?.let {
                val referenceShip = shipList.find { element -> element.isPositionPartOfShip(position) }
                ship = referenceShip
            }
            return FleetBattleField(
                ship,
                position,
                map["isVisible"] as Boolean
            )
        }
    }
}