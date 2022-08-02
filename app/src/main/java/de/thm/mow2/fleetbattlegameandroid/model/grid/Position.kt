package de.thm.mow2.fleetbattlegameandroid.model.grid

import java.io.Serializable

data class Position(
    val row: Int,
    val column: Int
): Serializable {
    companion object {

        fun toMap(pos: Position): Map<String, Any> {
            return mapOf(
                "row" to pos.row,
                "column" to pos.column
            )
        }

        fun toObject(map: Map<String, Any>): Position? {
            return Position(
                (map["row"] as Number).toInt(),
                (map["column"] as Number).toInt()
            )
        }
    }
}