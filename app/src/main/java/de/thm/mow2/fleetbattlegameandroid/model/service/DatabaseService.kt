package de.thm.mow2.fleetbattlegameandroid.model.service

import de.thm.mow2.fleetbattlegameandroid.model.core.FleetBattleGame
import de.thm.mow2.fleetbattlegameandroid.model.game.GameMode
import de.thm.mow2.fleetbattlegameandroid.model.game.User
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestoreQueryBuilder

class DatabaseService {
    companion object {
        fun saveFleetBattleGameObject(
            map: Map<String, Any>,
            callback: (FleetBattleGame?, String?) -> Unit
        ) {
            TPFirebaseFirestore.addDocument(FleetBattleGame.COLLECTION_NAME, map) { result, error ->
                result?.let { document ->
                    FleetBattleGame.toObject(document.data)?.let { gameObject ->
                        gameObject.documentId = document.documentId
                        callback(gameObject, null)
                    }
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun updateFleetBattleGameObject(
            id: String,
            map: Map<String, Any>,
            callback: (String?) -> Unit
        ) {
            TPFirebaseFirestore.updateDocument(FleetBattleGame.COLLECTION_NAME, id, map) { error ->
                callback(error)
            }
        }

        fun loadAllOpenFleetBattleGames(callback: (List<FleetBattleGame>?, String?) -> Unit) {
            val playerName1 =
                if (AuthenticationService.getUsername() != null) AuthenticationService.getUsername()!! else ""

            val query = TPFirebaseFirestoreQueryBuilder(FleetBattleGame.COLLECTION_NAME)
                .whereEqualTo("mode", GameMode.MULTIPLAYER.rawValue)
                .whereEqualTo("playerName2", "")
                .whereEqualTo("isFinished", false)
                .whereNotEqualTo("playerName1", playerName1)
            TPFirebaseFirestore.addCollectionSnapshotListener(query) { result, error ->
                result?.let { list ->
                    val games = mutableListOf<FleetBattleGame>()
                    for (element in list) {
                        FleetBattleGame.toObject(element.data)?.let { temp ->
                            temp.documentId = element.documentId
                            games.add(temp)
                        }
                    }
                    callback(games, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun getAllRunningGamesFromUser(callback: (List<FleetBattleGame>?, String?) -> Unit) {
            val playerName1 =
                if (AuthenticationService.getUsername() != null) AuthenticationService.getUsername()!! else ""

            val query = TPFirebaseFirestoreQueryBuilder(FleetBattleGame.COLLECTION_NAME)
                .whereEqualTo("mode", GameMode.MULTIPLAYER.rawValue)
                .whereEqualTo("isFinished", false)
                .whereArrayContains("playerNames", playerName1)
                .whereNotEqualTo("playerName2", "")
            TPFirebaseFirestore.addCollectionSnapshotListener(query) { result, error ->
                result?.let { list ->
                    val games = mutableListOf<FleetBattleGame>()
                    for (element in list) {
                        FleetBattleGame.toObject(element.data)?.let { temp ->
                            temp.documentId = element.documentId
                            games.add(temp)
                        }
                    }
                    callback(games, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun getAllOpenGameRequestsFromUser(callback: (List<FleetBattleGame>?, String?) -> Unit) {
            val playerName1 =
                if (AuthenticationService.getUsername() != null) AuthenticationService.getUsername()!! else ""

            val query = TPFirebaseFirestoreQueryBuilder(FleetBattleGame.COLLECTION_NAME)
                .whereEqualTo("mode", GameMode.MULTIPLAYER.rawValue)
                .whereEqualTo("playerName1", playerName1)
                .whereEqualTo("playerName2", "")
                .whereEqualTo("isFinished", false)
            TPFirebaseFirestore.addCollectionSnapshotListener(query) { result, error ->
                result?.let { list ->
                    val games = mutableListOf<FleetBattleGame>()
                    for (element in list) {
                        FleetBattleGame.toObject(element.data)?.let { temp ->
                            temp.documentId = element.documentId
                            games.add(temp)
                        }
                    }
                    callback(games, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun deleteGameById(id: String) {
            TPFirebaseFirestore.deleteDocument(FleetBattleGame.COLLECTION_NAME, id)
        }

        fun deleteOldSingleplayerGameOfUser() {
            val query = TPFirebaseFirestoreQueryBuilder(FleetBattleGame.COLLECTION_NAME)
                .whereEqualTo("mode", GameMode.SINGLEPLAYER.rawValue)
                .whereEqualTo("playerName1", AuthenticationService.getUsername() as String)

            TPFirebaseFirestore.getDocuments(query) { result, _ ->
                result?.let { list ->
                    for (element in list) {
                        deleteGameById(element.documentId)
                    }
                }
            }
        }

        fun deleteFinishedMultiplayerGamesOfUser() {
            val query = TPFirebaseFirestoreQueryBuilder(FleetBattleGame.COLLECTION_NAME)
                .whereEqualTo("mode", GameMode.MULTIPLAYER.rawValue)
                .whereEqualTo("isFinished", true)
                .whereArrayContains("playerNames", AuthenticationService.getUsername() as String)

            TPFirebaseFirestore.getDocuments(query) { result, _ ->
                result?.let { list ->
                    for (element in list) {
                        deleteGameById(element.documentId)
                    }
                }
            }
        }

        fun getOtherUsernames(callback: (List<User>?, String?) -> Unit) {
            val query = TPFirebaseFirestoreQueryBuilder(User.COLLECTION_NAME).whereNotEqualTo(
                "username", AuthenticationService.getUsername() as String
            )

            TPFirebaseFirestore.getDocuments(query) { result, error ->
                result?.let { list ->
                    val user = mutableListOf<User>()
                    for (element in list) {
                        User.toObject(element.documentId, element.data)?.let { tempUser ->
                            user.add(tempUser)
                        }
                    }
                    callback(user, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }
    }
}