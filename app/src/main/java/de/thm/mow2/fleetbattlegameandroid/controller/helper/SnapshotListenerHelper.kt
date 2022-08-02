package de.thm.mow2.fleetbattlegameandroid.controller.helper

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.service.SnapshotListenerInterface


class SnapshotListenerHelper: SnapshotListenerInterface {

    private val db = Firebase.firestore
    private var snapshotRegistration: ListenerRegistration? = null

    override fun startListening(id: String, callback: (FleetBattleGame?, String?) -> Unit) {
        removeListener()
        val docRef = db.collection(FleetBattleGame.COLLECTION_NAME).document(id)

        snapshotRegistration = docRef.addSnapshotListener { result, error ->
            result?.let { document ->
                if (document.exists()) {
                    FleetBattleGame.toObject(result.data!!)?.let { gameObject ->
                        gameObject.documentId = document.id
                        callback(gameObject, null)
                    }
                } else {
                    callback(null, "Data object doesn't exist anymore")
                }
            }
            error?.let { message ->
                callback(null, message.message)
            }

        }
    }

    override fun removeListener() {
        snapshotRegistration?.remove()
    }
}