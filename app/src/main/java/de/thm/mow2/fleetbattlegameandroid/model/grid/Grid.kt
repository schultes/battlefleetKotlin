package de.thm.mow2.fleetbattlegameandroid.model.grid

abstract class Grid {
    abstract var width: Int
    abstract var height: Int
    abstract var fields: List<List<Field>>

    fun getField(pos: Position): Field {
        return fields[pos.row][pos.column]
    }
}