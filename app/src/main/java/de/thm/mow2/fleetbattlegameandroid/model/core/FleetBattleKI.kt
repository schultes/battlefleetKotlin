package de.thm.mow2.fleetbattlegameandroid.model.core

import de.thm.mow2.fleetbattlegameandroid.model.game.DifficultyLevel
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.ship.Orientation
import de.thm.tp.library.random.TPRandom

class FleetBattleKI(
    private val difficultyLevel: DifficultyLevel,
    private var fleetBattleGrid: FleetBattleGrid
) {
    private val availablePositions: MutableList<MutableList<Boolean>>
    private val prioritizedPositions: MutableList<MutableList<Boolean>>
    private val followupPositions: MutableList<Position>

    init {

        val gridWidth = fleetBattleGrid.width
        val gridHeight = fleetBattleGrid.height
        availablePositions = MutableList(gridWidth) { MutableList(gridHeight) { true } }
        prioritizedPositions = MutableList(gridWidth) { MutableList(gridHeight) { false } }
        followupPositions = mutableListOf()

        if (difficultyLevel != DifficultyLevel.EASY) {
            generatePrioritizedPositions()
        }
    }

    fun getNextTurn(wasLastMoveByKI: Boolean): Position {
        val resultPos: Position
        val prioPos = getPosArrayFromGrid(prioritizedPositions)

        resultPos = when {
            followupPositions.size != 0 -> {
                getRandomPositionWithErrorProbability(followupPositions, wasLastMoveByKI)
            }
            prioPos.size != 0 -> {
                getRandomPositionWithErrorProbability(prioPos, wasLastMoveByKI)
            }
            else -> {
                val availablePos = getPosArrayFromGrid(availablePositions)
                getRandomPositionWithErrorProbability(availablePos, wasLastMoveByKI)
            }
        }

        return resultPos
    }

    // Get random position of list with possible error probability if ki already hit a ship in the last move
    private fun getRandomPositionWithErrorProbability(positions: MutableList<Position>, wasLastMoveByKI: Boolean): Position {
        val shouldKiHitWrongField = calculateErrorProbability()

        if (wasLastMoveByKI && shouldKiHitWrongField) {
            val shipList = positions.filter { position ->
                (fleetBattleGrid.fields[position.row][position.column] as FleetBattleField).ship != null
            }
            val waterList = positions.filter { position ->
                (fleetBattleGrid.fields[position.row][position.column] as FleetBattleField).ship == null
            }
            return if (waterList.isNotEmpty()) {
                waterList[TPRandom.int(0, waterList.size)]
            } else {
                shipList[TPRandom.int(0, shipList.size)]
            }
        }
        return positions[TPRandom.int(0, positions.size)]
    }

    // Calculate if ki should hit wrong field (random)
    private fun calculateErrorProbability(): Boolean {
        val randomValue = TPRandom.int(0,100)
        if (randomValue < 20) return false // 20% correct
        return true // 80% wrong
    }

    fun updateGrid(fleetBattleGrid: FleetBattleGrid) {

        val gridWidth = fleetBattleGrid.width
        val gridHeight = fleetBattleGrid.height

        for (y in 0 until fleetBattleGrid.height) { //update available positions
            for (x in 0 until fleetBattleGrid.width) {
                if (fleetBattleGrid.fields[y][x].isVisible) {
                    val field = fleetBattleGrid.fields[y][x] as FleetBattleField
                    availablePositions[x][y] = false
                    field.ship?.let {
                        if (it.submerged) {
                            for (row in (y - 1)..(y + 1)) {
                                for (col in (x - 1)..(x + 1)) {
                                    if (row >= 0 && col >= 0 && row < gridHeight && col < gridWidth) {
                                        availablePositions[col][row] = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        followupPositions.clear()
        for (ship in fleetBattleGrid.ships) {
            if (ship.submerged.not()) {
                val hits = mutableListOf<Position>()
                for (position in ship.getAllPositionsOfShip()) {
                    val field = fleetBattleGrid.getField(position)
                    if (field.isVisible) {
                        hits.add(position)
                    }
                }

                when {
                    hits.size == 1 -> {
                        val x = hits[0].column
                        val y = hits[0].row

                        if (x > 0 && availablePositions[x - 1][y]) {
                            followupPositions.add(Position(y, x - 1))
                        }
                        if (x < gridWidth - 1 && availablePositions[x + 1][y]) {
                            followupPositions.add(Position(y, x + 1))
                        }
                        if (y > 0 && availablePositions[x][y - 1]) {
                            followupPositions.add(Position(y - 1, x))
                        }
                        if (y < gridHeight - 1 && availablePositions[x][y + 1]) {
                            followupPositions.add(Position(y + 1, x))
                        }
                    }
                    hits.size > 1 -> {
                        if (ship.orientation == Orientation.HORIZONTAL) {
                            if (hits[0].column > 0 && availablePositions[hits[0].column - 1][hits[0].row]) {
                                followupPositions.add(Position(hits[0].row, hits[0].column - 1))
                            }
                            if (hits[hits.size - 1].column < gridWidth - 1 && availablePositions[hits[hits.size - 1].column + 1][hits[hits.size - 1].row]) {
                                followupPositions.add(
                                    Position(
                                        hits[hits.size - 1].row,
                                        hits[hits.size - 1].column + 1
                                    )
                                )
                            }
                        } else {
                            if (hits[0].row > 0 && availablePositions[hits[0].column][hits[0].row - 1]) {
                                followupPositions.add(Position(hits[0].row - 1, hits[0].column))
                            }
                            if (hits[hits.size - 1].row < gridHeight - 1 && availablePositions[hits[hits.size - 1].column][hits[hits.size - 1].row + 1]) {
                                followupPositions.add(
                                    Position(
                                        hits[hits.size - 1].row + 1,
                                        hits[hits.size - 1].column
                                    )
                                )
                            }
                        }
                    }
                }

                if (hits.size != 0) {
                    break
                }
            }
        }

        generatePrioritizedPositions()
    }

    private fun generatePrioritizedPositions() {
        val gridHeight = fleetBattleGrid.height
        val gridWidth = fleetBattleGrid.width

        when (difficultyLevel) {
            DifficultyLevel.MEDIUM -> {
                val borderOffsetX: Int = (gridWidth / 4) + if (gridWidth % 2 == 1) 1 else 0
                val borderOffsetY: Int = (gridHeight / 4) + if (gridHeight % 2 == 1) 1 else 0

                for (x in borderOffsetX until gridWidth - borderOffsetX) {
                    for (y in borderOffsetY until gridHeight - borderOffsetY) {
                        prioritizedPositions[x][y] = true
                    }
                }
            }
            DifficultyLevel.HARD -> {
                val ships = fleetBattleGrid.ships
                var maxLength = 0

                for (ship in ships) {
                    if (!ship.submerged && ship.length > maxLength) {
                        maxLength = ship.length
                    }
                }

                generateHardGrid(maxLength)
            }
        }

        for (y in 0 until fleetBattleGrid.height) {
            for (x in 0 until fleetBattleGrid.width) {
                if (availablePositions[x][y].not()) {
                    prioritizedPositions[x][y] = false
                }
            }
        }
    }

    private fun generateHardGrid(maxLength: Int) {
        for (y in 0 until fleetBattleGrid.height) {
            for (x in 0 until fleetBattleGrid.width) {
                prioritizedPositions[x][y] = false
            }
            for (x in (0 - y) until fleetBattleGrid.width step maxLength) {
                if (x >= 0) {
                    prioritizedPositions[x][y] = true
                }
            }
        }
    }

    private fun getPosArrayFromGrid(list: MutableList<MutableList<Boolean>>): MutableList<Position> {
        val resultList = mutableListOf<Position>()

        for (y in 0 until list[0].size) {
            for (x in 0 until list.size) {
                if (list[x][y]) {
                    resultList.add(Position(y, x))
                }
            }
        }

        return resultList
    }

}