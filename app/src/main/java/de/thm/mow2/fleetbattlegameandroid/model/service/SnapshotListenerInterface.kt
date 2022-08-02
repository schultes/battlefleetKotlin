package de.thm.mow2.fleetbattlegameandroid.model.service

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame

interface SnapshotListenerInterface {
    fun startListening(id: String, callback: (FleetBattleGame?, String?) -> Unit)
    fun removeListener()
}