package world.gregs.voidps.network.login

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.network.Response

/**
 * Checks account credentials are valid
 */
class PasswordManager(private val loader: AccountLoader) {

    fun validate(username: String, password: String): Int {
        if (username.length > 12) {
            return Response.LOGIN_SERVER_REJECTED_SESSION
        }
        val passwordHash = loader.password(username)
        try {
            if (loader.exists(username) && passwordHash == null) {
                // Failed to find accounts password despite account existing
                return Response.ACCOUNT_DISABLED
            }
            if (loader.exists(username) && !BCrypt.checkpw(password, passwordHash)) {
                return Response.INVALID_CREDENTIALS
            }
        } catch (e: IllegalArgumentException) {
            return Response.COULD_NOT_COMPLETE_LOGIN
        }
        return Response.SUCCESS
    }

    fun encrypt(username: String, password: String): String {
        val passwordHash = loader.password(username)
        if (passwordHash != null) {
            return passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

}