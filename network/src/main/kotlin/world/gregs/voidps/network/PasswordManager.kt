package world.gregs.voidps.network

import org.mindrot.jbcrypt.BCrypt

class PasswordManager(private val loader: AccountLoader) {

    fun validate(username: String, password: String): Int {
        if (username.length > 12) {
            return Response.LOGIN_SERVER_REJECTED_SESSION
        }
        val passwordHash = loader.password(username)
        if (passwordHash != null && !BCrypt.checkpw(password, passwordHash)) {
            return Response.INVALID_CREDENTIALS
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