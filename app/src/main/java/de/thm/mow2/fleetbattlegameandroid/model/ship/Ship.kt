package de.thm.mow2.fleetbattlegameandroid.model.ship

import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import java.io.Serializable

class Ship(
    length: Int,
    orientation: Orientation,
    startPosition: Position,
    submerged: Boolean
) : Serializable {
    val length: Int
    val orientation: Orientation
    val startPosition: Position
    var submerged: Boolean = false

    init {
        this.length = length
        this.orientation = orientation
        this.startPosition = startPosition
        this.submerged = submerged
    }

    fun isPositionPartOfShip(pos: Position): Boolean {
        return getAllPositionsOfShip().any { it.column == pos.column && it.row == pos.row }
    }

    fun getAllPositionsOfShip(): List<Position> {
        var positions = mutableListOf<Position>()
        when (orientation) {
            Orientation.HORIZONTAL -> {
                for (i in 0 until length) {
                    positions += Position(startPosition.row, startPosition.column + i)
                }
            }
            Orientation.VERTICAL -> {
                for (i in 0 until length) {
                    positions += Position(startPosition.row + i, startPosition.column)
                }
            }
        }
        return positions
    }

    fun getLastPositionOfShip(): Position {
        return if (orientation == Orientation.HORIZONTAL) {
            Position(startPosition.row, startPosition.column + length - 1)
        } else {
            Position(startPosition.row + length - 1, startPosition.column)
        }
    }

    companion object {
        fun multipleToMap(ships: List<Ship>): List<Map<String, Any>> {
            val shipMap = arrayListOf<Map<String, Any>>()
            ships.forEach { ship ->
                shipMap.add(Ship.toMap(ship))
            }
            return shipMap
        }

        fun toMap(ship: Ship): Map<String, Any> {
            return mapOf(
                "length" to ship.length,
                "orientation" to ship.orientation.rawValue,
                "startPosition" to Position.toMap(ship.startPosition),
                "submerged" to ship.submerged
            )
        }

        fun multipleToObject(map: List<Map<String, Any>>): List<Ship> {
            val shipMap = arrayListOf<Ship>()
            map.forEach { ship ->
                shipMap.add(Ship.toObject(ship)!!)
            }
            return shipMap
        }

        fun toObject(map: Map<String, Any>): Ship? {
            return Ship(
                (map["length"] as Number).toInt(),
                Orientation(map["orientation"]!! as String)!!,
                Position.toObject(map["startPosition"] as Map<String, Any>)!!,
                map["submerged"] as Boolean
            )
        }
    }
}