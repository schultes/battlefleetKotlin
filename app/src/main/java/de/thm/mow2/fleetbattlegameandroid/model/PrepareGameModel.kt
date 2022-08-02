package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleField
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGameDetails
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGrid
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.grid.Position
import de.thm.mow2.fleetbattlegameandroid.model.helper.GridHelperObject
import de.thm.mow2.fleetbattlegameandroid.model.helper.PlaceShipFeedback
import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.service.DatabaseService
import de.thm.mow2.fleetbattlegameandroid.model.service.GameService
import de.thm.mow2.fleetbattlegameandroid.model.service.ShipPlacementService
import de.thm.mow2.fleetbattlegameandroid.model.ship.Orientation
import de.thm.mow2.fleetbattlegameandroid.model.ship.Ship

interface PrepareGameController {
    fun showErrorMessage(message: String)
    fun startGameActivity(game: FleetBattleGame)
    fun startGameResultActivity(game: FleetBattleGame)
    fun setShipList(shipList: List<Ship>)
    fun showFeedbackAfterClick(feedback: PlaceShipFeedback)
}

class PrepareGameModel(private val controller: PrepareGameController) {

    fun initGridHelperObjects(width: Int, height: Int): ArrayList<GridHelperObject> {
        val cells = ArrayList<GridHelperObject>(width * height)
        for (row in 0 until width) {
            for (column in 0 until height) {
                val gridHelper = GridHelperObject(
                    Position(row, column),
                    false
                )
                cells.add(gridHelper)
            }
        }
        return cells
    }

    fun getRandomPlacedShips(gameDetails: FleetBattleGameDetails): ArrayList<GridHelperObject> {
        val cells = ShipPlacementService.getGridForKi(gameDetails)
        return cells.flatten() as ArrayList<GridHelperObject>
    }


    private fun getGameDetailsMap(gameDetails: FleetBattleGameDetails): MutableMap<Int, Int> {
        val gameConfig = mutableMapOf<Int, Int>()
        for (shipDetails in gameDetails.shipDetailList) {
            gameConfig[shipDetails.length] = shipDetails.numberOfThisType
        }
        return gameConfig
    }

    private fun hasGridHelperShip(
        cells: ArrayList<GridHelperObject>,
        position: Position
    ): Boolean? {
        return cells.find { it.position == position }?.isShipSet
    }

    private fun isEnoughSpaceAroundShip(
        ship: Ship,
        cells: ArrayList<GridHelperObject>,
        gridWidth: Int
    ): Boolean {
        if (ship.orientation == Orientation.HORIZONTAL) {
            val shipRow = ship.startPosition.row
            // column-index of last field of ship
            val lastCol = ship.startPosition.column + ship.length - 1

            // Iterate over the columns of the ship (horizontal)
            for (j in ship.startPosition.column..lastCol) {
                val position = Position(shipRow + 1, j)
                val helper = hasGridHelperShip(cells, position)
                if (gridWidth > shipRow + 1 && helper == true) return false
            }
            // Corner (right down & left down)
            val helper = hasGridHelperShip(cells, Position(shipRow + 1, lastCol + 1))
            if ((lastCol + 1 < gridWidth) && (shipRow + 1 < gridWidth) && helper == true) return false

            val helper2 =
                hasGridHelperShip(cells, Position(shipRow + 1, ship.startPosition.column - 1))
            if ((ship.startPosition.column > 0) && (shipRow + 1 < gridWidth) && helper2 == true) return false

        } else {
            val shipCol = ship.startPosition.column
            val lastRow = ship.startPosition.row + ship.length - 1

            for (i in ship.startPosition.row..lastRow) {
                val helper = hasGridHelperShip(cells, Position(i, shipCol - 1))
                if (shipCol > 0 && helper == true) return false
                val helper2 = hasGridHelperShip(cells, Position(i, shipCol + 1))
                if (gridWidth > shipCol + 1 && helper2 == true) return false
            }
            // Corner (right down & left down)
            val helper = hasGridHelperShip(cells, Position(lastRow + 1, shipCol + 1))
            if ((lastRow + 1 < gridWidth) && (shipCol + 1 < gridWidth - 1) && helper == true) return false
            val helper2 = hasGridHelperShip(cells, Position(lastRow + 1, shipCol - 1))
            if ((lastRow + 1 < gridWidth) && (shipCol > 0) && helper2 == true) return false
        }
        return true
    }


    private fun getAllPositionsOfPossibleShip(
        cells: ArrayList<GridHelperObject>,
        gridWidth: Int,
        isHorizontal: Boolean,
        startPosition: Position
    ): List<Position> {
        val positions = mutableListOf(startPosition)
        var i = startPosition.row
        var j = startPosition.column
        if (isHorizontal) {
            var position = Position(i, j + 1)
            var helper = hasGridHelperShip(cells, position)
            while (j + 1 < gridWidth && helper == true) {
                positions += Position(i, j + 1)
                j += 1
                position = Position(i, j + 1)
                helper = hasGridHelperShip(cells, position)
            }
        } else {
            var position = Position(i + 1, j)
            var helper = hasGridHelperShip(cells, position)
            while (i + 1 < gridWidth && helper == true) {
                positions += Position(i + 1, j)
                i += 1
                position = Position(i + 1, j)
                helper = hasGridHelperShip(cells, position)
            }
        }
        return positions
    }


    private fun isFieldContainedInShip(
        shipList: ArrayList<Ship>,
        pos: Position
    ): Boolean {
        for (item in shipList) {
            if (item.isPositionPartOfShip(pos)) return true
        }
        return false
    }


    private fun checkIsHorizontal(
        cells: ArrayList<GridHelperObject>,
        row: Int,
        column: Int,
        gridWidth: Int
    ): Boolean {
        val position = Position(row, column + 1)
        val helper = hasGridHelperShip(cells, position)
        if (column + 1 < gridWidth && helper == true)
            return true
        return false
    }


    fun checkGridValidation(
        gameDetails: FleetBattleGameDetails,
        cells: ArrayList<GridHelperObject>,
        onConfirm: Boolean
    ) {
        val possibleMap = mutableMapOf<Int, Int>()
        val shipList = arrayListOf<Ship>()
        val height = gameDetails.gridHeight
        val width = gameDetails.gridWidth
        val gameConfig = getGameDetailsMap(gameDetails)
        var errorMessageForImmediatelyFeedback = ""

        // Iterate over each field of grid
        for (row in 0 until height) {
            for (column in 0 until width) {
                val position = Position(row, column)
                val helper = hasGridHelperShip(cells, position)
                // Ship part found: boolean is true and is not part of any other already found ship
                if (helper == true && !isFieldContainedInShip(shipList, Position(row, column))) {
                    val isHorizontal = checkIsHorizontal(cells, row, column, width)

                    val positions = getAllPositionsOfPossibleShip(
                        cells,
                        width,
                        isHorizontal,
                        Position(row, column)
                    )
                    val orientation =
                        if (isHorizontal) Orientation.HORIZONTAL else Orientation.VERTICAL
                    val ship = Ship(positions.size, orientation, Position(row, column), false)

                    shipList += ship

                    if (!isEnoughSpaceAroundShip(ship, cells, width)) {
                        if (onConfirm) {
                            controller.showErrorMessage(
                                "Schiffe dürfen sich nicht berühren oder \n" +
                                        " diagonal angeordnet werden"
                            )
                            return
                        }
                        errorMessageForImmediatelyFeedback =
                            "Schiffe dürfen sich nicht berühren oder \n diagonal angeordnet werden"
                    }

                    // Add in possibleMap
                    if (possibleMap[positions.size] != null) {
                        possibleMap[positions.size] = possibleMap[positions.size]!! + 1
                    } else {
                        possibleMap[positions.size] = 1
                    }

                    // Is ship with this size and count allowed
                    if (gameConfig[positions.size] == null) {
                        if (onConfirm) {
                            controller.showErrorMessage("Ungültiger Schifftyp\ngefunden")
                            return
                        }
                    } else if (gameConfig[positions.size]!! < possibleMap[positions.size]!!) {
                        val nameOfShip =
                            (gameDetails.shipDetailList.find { element -> element.length == positions.size })!!.name
                        if (onConfirm) {
                            controller.showErrorMessage("Zu viele Schiffe vom Typ\n$nameOfShip")
                            return
                        }
                    }
                }
            }
        }
        if (gameConfig == possibleMap) {
            if (onConfirm) {
                controller.showErrorMessage("")
                controller.setShipList(shipList)
            }
        } else {
            if (onConfirm) {
                controller.showErrorMessage("Es fehlen Schiffe")
            }
        }
        if (!onConfirm) {
            controller.showFeedbackAfterClick(
                PlaceShipFeedback(
                    gameConfig,
                    possibleMap,
                    errorMessageForImmediatelyFeedback
                )
            )
        }
    }


    private fun createFields(
        shipList: List<Ship>,
        gameDetails: FleetBattleGameDetails
    ): List<List<FleetBattleField>> {
        val fields = ArrayList<ArrayList<FleetBattleField>>()

        for (row in 0 until gameDetails.gridHeight) {
            val tempList = arrayListOf<FleetBattleField>()
            for (column in 0 until gameDetails.gridWidth) {
                val position = Position(row, column)
                var ship: Ship? = null
                for (element in shipList) {
                    if (element.isPositionPartOfShip(position)) {
                        ship = element
                        break
                    }
                }
                tempList.add(FleetBattleField(ship, position, false))
            }
            fields += tempList
        }
        return fields
    }

    private fun extractShipsFromGrid(
        gameDetails: FleetBattleGameDetails,
        cells: ArrayList<GridHelperObject>
    ): ArrayList<Ship> {
        val shipList = arrayListOf<Ship>()
        val width = gameDetails.gridWidth
        val height = gameDetails.gridHeight

        for (row in 0 until height) {
            for (column in 0 until width) {
                val position = Position(row, column)
                val helper = hasGridHelperShip(cells, position)
                // Ship part found: boolean is true and is not part of any other already found ship
                if (helper == true && !isFieldContainedInShip(shipList, Position(row, column))) {
                    val isHorizontal = checkIsHorizontal(cells, row, column, width)

                    val positions = getAllPositionsOfPossibleShip(
                        cells,
                        width,
                        isHorizontal,
                        Position(row, column)
                    )
                    val orientation =
                        if (isHorizontal) Orientation.HORIZONTAL else Orientation.VERTICAL
                    val ship = Ship(positions.size, orientation, Position(row, column), false)

                    shipList += ship
                }
            }
        }
        return shipList
    }

    private fun generateGridForKi(
        gameDetails: FleetBattleGameDetails,
        name: String
    ): FleetBattleGrid {
        val gridKi = getRandomPlacedShips(gameDetails)
        val shipListKi = extractShipsFromGrid(gameDetails, gridKi)
        val fieldsKi = createFields(shipListKi, gameDetails)

        return FleetBattleGrid(
            gameDetails,
            gameDetails.gridWidth,
            gameDetails.gridHeight,
            fieldsKi,
            name,
            shipListKi
        )
    }

    fun saveGridInGame(
        shipList: List<Ship>,
        gameDetails: FleetBattleGameDetails,
        game: FleetBattleGame
    ) {
        val fields = createFields(shipList, gameDetails)
        AuthenticationService.getUsername()?.let { username ->
            val grid = FleetBattleGrid(
                gameDetails,
                gameDetails.gridWidth,
                gameDetails.gridHeight,
                fields,
                username,
                shipList
            )
            val changedAttribute = mutableMapOf<String, Any>()
            if (GameService.isUserPlayer1(game)) {
                game.gridFromPlayer1 = grid
                changedAttribute["gridFromPlayer1"] = FleetBattleGrid.toMap(grid)
            } else {
                game.gridFromPlayer2 = grid
                changedAttribute["gridFromPlayer2"] = FleetBattleGrid.toMap(grid)
            }

            if (game.mode == GameMode.SINGLEPLAYER) {
                val nameForKI = "CPU"
                game.playerName2 = nameForKI
                game.gridFromPlayer2 = generateGridForKi(gameDetails, nameForKI)
                changedAttribute["playerName2"] = nameForKI
                changedAttribute["playerNames"] = listOf(game.playerName1, nameForKI)
                changedAttribute["gridFromPlayer2"] = FleetBattleGrid.toMap(game.gridFromPlayer2!!)
            }

            DatabaseService.updateFleetBattleGameObject(
                game.documentId!!,
                changedAttribute
            ) { errorMessage ->
                if (errorMessage == null) {
                    controller.startGameActivity(game)
                } else {
                    controller.startGameResultActivity(game)
                }
            }
        }
    }
}
