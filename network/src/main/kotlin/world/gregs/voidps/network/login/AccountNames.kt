package world.gregs.voidps.network.login

import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator

/**
 * Account names are either a username of up to 12 characters or the email address the account was registered with
 */
object AccountNames {
    const val MAX_USERNAME_LENGTH = 12

    fun isEmail(username: String): Boolean = '@' in username

    fun valid(username: String): Boolean {
        if (isEmail(username)) {
            return RegistrationValidator.email(username) == RegistrationResponse.SUCCESS
        }
        return username.length in 1..MAX_USERNAME_LENGTH
    }

    /**
     * Email addresses are stored and compared in lowercase
     */
    fun normalise(username: String): String = if (isEmail(username)) username.lowercase() else username
}
