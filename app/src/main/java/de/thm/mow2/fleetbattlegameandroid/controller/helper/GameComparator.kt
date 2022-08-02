package de.thm.mow2.fleetbattlegameandroid.controller.helper

import de.thm.mow2.fleetbattlegameandroid.model.game.Game

class GameComparator: Comparator<Game> {
    override fun compare(game1: Game, game2: Game): Int {
        val dateTime1 = DateTimeHelper.getDateFromString(game1.createdAt)
        val dateTime2 = DateTimeHelper.getDateFromString(game2.createdAt)

        return dateTime1.compareTo(dateTime2)
    }
}