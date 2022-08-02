package de.thm.mow2.fleetbattlegameandroid.model.core

import de.thm.mow2.fleetbattlegameandroid.model.ship.ShipDetails

data class FleetBattleGameDetails(
    val id: String,
    val name: String,
    val gridWidth: Int,
    val gridHeight: Int,
    val shipDetailList: List<ShipDetails>
)