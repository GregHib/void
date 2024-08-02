package world.gregs.voidps.network.login

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.network.Response

/**
 * Checks account credentials are valid
 */
class PasswordManager(private val account: AccountLoader) {

    fun validate(username: String, password: String): Int {
        if (username.length > 12) {
            return Response.LOGIN_SERVER_REJECTED_SESSION
        }
        val passwordHash = account.password(username)
        if (!account.exists(username)) {
            if (passwordHash != null) {
                // Failed to find account file despite AccountDefinition exists in memory (aka existed on startup)
                return Response.ACCOUNT_DISABLED
            }
            return Response.SUCCESS
        }
        try {
            if (passwordHash == null) {
                // Failed to find accounts password despite account file existing (created since startup)
                return Response.ACCOUNT_DISABLED
            }
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