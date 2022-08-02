package de.thm.mow2.fleetbattlegameandroid.model.service

import de.thm.mow2.fleetbattlegameandroid.model.game.User
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthenticationUser
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestoreDocument
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestoreQueryBuilder

class AuthenticationService {
    companion object {
        fun signIn(
            email: String,
            password: String,
            callback: (TPFirebaseAuthenticationUser?, String?) -> Unit
        ) {
            TPFirebaseAuthentication.signIn(email, password) { result, error ->
                result?.let { user ->
                    callback(user, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun signOut() {
            if (TPFirebaseAuthentication.isSignedIn()) {
                TPFirebaseAuthentication.signOut()
            }
        }

        fun getUsername(): String? {
            return TPFirebaseAuthentication.getUser()?.displayName
        }

        fun signUp(
            email: String,
            password: String,
            displayName: String,
            callback: (TPFirebaseAuthenticationUser?, String?) -> Unit
        ) {
            TPFirebaseAuthentication.signUp(email, password, displayName) { result, error ->
                result?.let { user ->
                    callback(user, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun getUsersByDisplayname(
            displayName: String,
            callback: (ArrayList<TPFirebaseFirestoreDocument>?, String?) -> Unit
        ) {
            val query = TPFirebaseFirestoreQueryBuilder(User.COLLECTION_NAME).whereEqualTo(
                "username",
                displayName
            )
            TPFirebaseFirestore.getDocuments(query) { result, error ->
                result?.let { list ->
                    callback(list, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }
    }
}