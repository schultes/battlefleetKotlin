package de.thm.mow2.fleetbattlegameandroid.model.ship

enum class Orientation(val rawValue: String) {
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertikal");

    companion object {
        operator fun invoke(rawValue: String) = values().firstOrNull {
            it.rawValue == rawValue
        }
    }
}