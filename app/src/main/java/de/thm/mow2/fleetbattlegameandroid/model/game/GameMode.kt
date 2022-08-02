package de.thm.mow2.fleetbattlegameandroid.model.game

enum class GameMode(val rawValue: String) {
    SINGLEPLAYER("Einzelspieler"),
    MULTIPLAYER("Mehrspieler");

    companion object {
        operator fun invoke(rawValue: String) = values().firstOrNull {
            it.rawValue == rawValue
        }
    }

}