package de.thm.mow2.fleetbattlegameandroid.model.helper

data class PlaceShipFeedback(
    var gameConfig: MutableMap<Int, Int>,
    var currentShipMap: MutableMap<Int, Int>,
    var errorMessage: String
)