package de.thm.mow2.fleetbattlegameandroid.model.service

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.helper.GridHelperObject
import de.thm.mow2.fleetbattlegameandroid.model.helper.ShipHelperObject
import de.thm.mow2.fleetbattlegameandroid.model.ship.Orientation
import de.thm.tp.library.random.TPRandom

class ShipPlacementService {
    companion object {
        private var grid = arrayOf<Array<GridHelperObject>>()
        private var dummyShipList = mutableListOf<ShipHelperObject>()
        private var gridWidth = 0
        private var gridHeight = 0


        // Init data
        private fun initData(fleetBattleGameDetails: FleetBattleGameDetails) {
            gridWidth = fleetBattleGameDetails.gridWidth
            gridHeight = fleetBattleGameDetails.gridHeight
            grid = Array(gridHeight) { i ->
                Array(gridWidth) { j ->
                    GridHelperObject(
                        Position(i, j),
                        false
                    )
                }
            }

            // Init ships with random orientation
            dummyShipList.clear()
            for (shipType in fleetBattleGameDetails.shipDetailList) {
                for (i in 0 until shipType.numberOfThisType) {
                    val randomNumber = TPRandom.int(0, 2)
                    val randomOrientation =
                        if (randomNumber == 0) Orientation.HORIZONTAL else Orientation.VERTICAL
                    val ship = ShipHelperObject(shipType.length, randomOrientation)
                    dummyShipList.add(ship)
                }
            }
            // Sort ships with descending length (big ships first)
            dummyShipList.sortByDescending { ship -> ship.length }
        }

        fun getGridForKi(fleetBattleGameDetails: FleetBattleGameDetails): Array<Array<GridHelperObject>> {
            var result = false
            while (!result) {
                initData(fleetBattleGameDetails)
                result = placeDummyShips()
            }
            return grid
        }

        // Helper funcion: calculate positions around one special position (for place around the ships)
        private fun getAllPositionsAroundPosition(pos: Position): List<Position> {
            val list = mutableListOf<Position>()
            val row = pos.row
            val col = pos.column
            for (r in (row - 1)..(row + 1)) {
                for (c in (col - 1)..(col + 1)) {
                    if (r != -1 && c != -1) {
                        list.add(Position(r, c))
                    }
                }
            }
            return list
        }

        // Calculate all positions of and around ship (this positions are not available for other ships anymore)
        private fun getAllOfPositionsOfAndAroundShip(
            startPosition: Position,
            ship: ShipHelperObject
        ): List<Position> {
            val list = mutableListOf(startPosition)
            when (ship.orientation) {
                Orientation.HORIZONTAL -> {
                    for (i in startPosition.column until startPosition.column + ship.length) {
                        list += getAllPositionsAroundPosition(Position(startPosition.row, i))
                    }
                }
                Orientation.VERTICAL -> {
                    for (i in startPosition.row until startPosition.row + ship.length) {
                        list += getAllPositionsAroundPosition(Position(i, startPosition.column))
                    }
                }
            }
            // Remove duplicates
            return list.distinct()
        }

        // Helper funcion for init: Create list with all positions of the grid (row based)
        // Multiple rows -> each row can have multiple list parts with free positions
        // Horizontal: based on rows; Vertical: based on columns
        private fun setHelperListOfPositions(orientation: Orientation): MutableList<MutableList<MutableList<Position>>> {
            val firstListIndex =
                if (orientation == Orientation.HORIZONTAL) gridHeight else gridWidth
            val secondListIndex =
                if (orientation == Orientation.HORIZONTAL) gridWidth else gridHeight
            val completeList = mutableListOf<MutableList<MutableList<Position>>>()

            for (r in 0 until firstListIndex) {
                val list = mutableListOf<MutableList<Position>>()
                val rowList = mutableListOf<Position>()
                for (c in 0 until secondListIndex) {
                    if (orientation == Orientation.HORIZONTAL) {
                        rowList.add(Position(r, c))
                    } else {
                        rowList.add(Position(c, r))
                    }

                }
                list.add(rowList)
                completeList.add(list)
            }
            // list of all rows or columns (at first with one list of positions inside)
            return completeList

        }

        // Random startPosition for ships
        private fun choosePossibleStartPosition(
            list: List<List<List<Position>>>,
            ship: ShipHelperObject
        ): Position? {
            // Random number for placing ship at the start or end of grid
            val randomSearchAtStartOrEnd = TPRandom.int(0, 2)
            // All possible row indices
            var indexList = (list.indices).toList()
            // Sort list for priorities
            indexList = if (randomSearchAtStartOrEnd == 0) indexList else indexList.reversed()
            val possiblePositionsOfElement: List<Position>
            for (i in indexList) {
                // Iterate over sublists of row
                for (rowParts in list[i]) {
                    // One sublist of the row with free positions (must be long enough for ship for placing)
                    if (rowParts.size >= ship.length) {
                        possiblePositionsOfElement = rowParts
                        // Possible startpositions in sublist of row
                        val possibleStartPositionCount = rowParts.size - ship.length
                        // Random startposition
                        val randomStartPosition = TPRandom.int(0, possibleStartPositionCount + 1)
                        return possiblePositionsOfElement.elementAt(randomStartPosition)
                    }
                }
            }
            // No possible position found (all sublists not long enough)
            return null
        }

        // Remove all reserved positions of list (split list in sublists if they lost a position)
        private fun updateListOfPositions(
            original: MutableList<MutableList<MutableList<Position>>>,
            positions: List<Position>
        ): MutableList<MutableList<MutableList<Position>>> {
            val completeList: MutableList<MutableList<MutableList<Position>>> =
                original.toMutableList()
            // Iterate over list with positions
            for (rowIndex in 0 until original.size) {
                val row = completeList[rowIndex]
                // sublists of list
                for (partIndex in 0 until row.size) {
                    // sublist
                    val part = row[partIndex]
                    // Check if sublist contains reserved positions
                    val foundElements = part.intersect(positions)
                    // Get indices of reserved positions
                    val indexOfValues = foundElements.map { el -> part.indexOf(el) }
                    // Reserved positions found in sublist
                    if (indexOfValues.isNotEmpty()) {
                        // split list
                        var first = part.subList(0, indexOfValues[0])
                        var second = part.subList(indexOfValues[0], part.size)
                        // Remove all reserved positions
                        first = first.filter { el -> !foundElements.contains(el) }.toMutableList()
                        second = second.filter { el -> !foundElements.contains(el) }.toMutableList()
                        // Overwrite list with new sublists
                        (completeList[rowIndex]).removeAt(partIndex)
                        (completeList[rowIndex]).add(partIndex, first)
                        (completeList[rowIndex]).add(partIndex + 1, second)
                    }
                }
            }
            return completeList
        }

        // Place all ships of the gameDetails on the grid
        private fun placeDummyShips(): Boolean {
            // Helper lists for all rows and columns (with sublists of positions)
            var listOfRows: MutableList<MutableList<MutableList<Position>>> =
                setHelperListOfPositions(Orientation.HORIZONTAL)
            var listOfCols: MutableList<MutableList<MutableList<Position>>> =
                setHelperListOfPositions(Orientation.VERTICAL)

            // Set ships
            for (ship in dummyShipList) {
                // Place a ship in row (horizontal) or in column (vertical)
                val relevantList =
                    if (ship.orientation == Orientation.HORIZONTAL) listOfRows else listOfCols
                // Calculate possible start position for ship
                val startPosition = choosePossibleStartPosition(relevantList, ship) ?: return false
                // No position found -> break and try again

                // Set values of position to true (= ship)
                setShipInGrid(ship, startPosition)
                // Remove reserved positions for space between ships
                val alreadyUsedPositions: List<Position> = getAllOfPositionsOfAndAroundShip(startPosition, ship)
                listOfRows = updateListOfPositions(listOfRows, alreadyUsedPositions)
                listOfCols = updateListOfPositions(listOfCols, alreadyUsedPositions)

            }
            // All ships are places
            return true
        }

        // Set ship in Grid (set value to true)
        private fun setShipInGrid(ship: ShipHelperObject, startPosition: Position) {
            when (ship.orientation) {
                Orientation.HORIZONTAL -> {
                    val fixRow = startPosition.row
                    for (i in 0 until ship.length) {
                        grid[fixRow][startPosition.column + i].isShipSet = true
                    }
                }
                Orientation.VERTICAL -> {
                    val fixCol = startPosition.column
                    for (i in 0 until ship.length) {
                        grid[startPosition.row + i][fixCol].isShipSet = true
                    }
                }
            }
        }
    }
}

