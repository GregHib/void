package world.gregs.voidps.network.login

import de.mkammerer.argon2.Argon2Factory
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
        if (passwordHash == null) {
            return Response.ACCOUNT_DISABLED
        }

        return try {
            when {
                passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") ->
                    if (BCrypt.checkpw(password, passwordHash)) Response.SUCCESS else Response.INVALID_CREDENTIALS

                passwordHash.startsWith("\$argon2i\$") ->
                    if (Argon2Factory.create().verify(passwordHash, password.toCharArray())) Response.SUCCESS else Response.INVALID_CREDENTIALS

                else -> Response.COULD_NOT_COMPLETE_LOGIN // Unknown hash type
            }
        } catch (e: Exception) {
            Response.COULD_NOT_COMPLETE_LOGIN
        }
    }

    fun encrypt(username: String, password: String): String {
        val passwordHash = account.password(username)
        if (passwordHash != null) {
            return passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

}