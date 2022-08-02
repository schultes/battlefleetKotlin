package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService
import de.thm.mow2.fleetbattlegameandroid.model.game.User
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore

interface RegisterController {
    fun redirectToMenuActivity()
    fun showErrorMessage(message: String)
}

class RegisterModel(private val controller: RegisterController) {

    fun onRegisterClicked(email: String, password: String, displayName: String) {

        AuthenticationService.getUsersByDisplayname(displayName) { result, error ->
            result?.let { userlist ->
                if (userlist.isEmpty()) {
                    AuthenticationService.signUp(email, password, displayName) { result, error ->
                        result?.let { user ->
                            user.displayName?.let { displayName ->
                                TPFirebaseFirestore.addDocument(
                                    User.COLLECTION_NAME,
                                    User.toMap(User(null, displayName))
                                ) { result, _ ->
                                    result?.let {
                                        controller.redirectToMenuActivity()
                                    }
                                }
                            }
                        }
                        error?.let { message ->
                            controller.showErrorMessage(message)
                        }
                    }
                } else {
                    controller.showErrorMessage("DisplayName is already used.")
                }
            }

            error?.let { _ ->
                controller.showErrorMessage("Registration failed.")
            }
        }
    }

}