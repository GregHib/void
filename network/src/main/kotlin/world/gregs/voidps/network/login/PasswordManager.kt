package world.gregs.voidps.network.login

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.network.Response

/**
 * Checks account credentials are valid
 */
class PasswordManager(private val account: AccountLoader) {

    fun validate(username: String, password: String): Int {
        if (!AccountNames.valid(username)) {
            // The client shows 11 as a weak password warning, 3 is "Invalid username or password"
            return Response.INVALID_CREDENTIALS
        }
        val passwordHash = account.password(username)
        if (passwordHash == null) {
            if (account.used(username)) {
                // Username already in use as a display name
                return Response.INVALID_CREDENTIALS
            }
            if (account.exists(username)) {
                // Failed to find accounts password despite account file existing (created since startup)
                return Response.ACCOUNT_DISABLED
            }
            return Response.SUCCESS
        }
        // Accounts created since startup are only in memory until their first save, so the hash is checked whether or not a file exists yet
        try {
            if (BCrypt.checkpw(password, passwordHash)) {
                return Response.SUCCESS
            }
        } catch (e: IllegalArgumentException) {
            return Response.COULD_NOT_COMPLETE_LOGIN
        }
        return Response.INVALID_CREDENTIALS
    }

    fun encrypt(username: String, password: String): String {
        val passwordHash = account.password(username)
        if (passwordHash != null) {
            return passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}
