package de.thm.mow2.fleetbattlegameandroid.model.game

import de.thm.mow2.fleetbattlegameandroid.model.grid.Position

abstract class Game {
    abstract var createdAt: String
    abstract var gridHeight: Int
    abstract var gridWidth: Int
    abstract var mode: GameMode
    abstract var playerName1: String
    abstract var playerName2: String?
    abstract var isFinished: Boolean
    abstract var winner: String?
    abstract var usersTurn: Int
    abstract var difficultyLevel: DifficultyLevel?
    abstract var lastMoveBy: String?
}