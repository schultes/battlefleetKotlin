package de.thm.mow2.fleetbattlegameandroid.controller.model

import de.thm.mow2.fleetbattlegameandroid.model.service.AuthenticationService

interface LoginController {
    fun redirectToMenuActivity()
    fun showErrorMessage(message: String)
}

class LoginModel(private val controller: LoginController) {

    fun onLoginClicked(email: String, password: String) {
        AuthenticationService.signIn(email, password) { result, error ->
            result?.let { user ->
                controller.redirectToMenuActivity()
            }
            error?.let { message ->
                controller.showErrorMessage(message)
            }
        }
    }

}