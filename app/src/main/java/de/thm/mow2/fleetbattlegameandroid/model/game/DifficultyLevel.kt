package de.thm.mow2.fleetbattlegameandroid.model.game

enum class DifficultyLevel(val rawValue: String) {
    EASY("Einfach"),
    MEDIUM("Mittel"),
    HARD("Schwer");

    companion object {
        operator fun invoke(rawValue: String) = values().firstOrNull {
            it.rawValue == rawValue
        }
    }

}