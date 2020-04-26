package rs.dusk.cache.secure

import com.github.michaelbull.logging.InlineLogger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object Encryption {
    private val secureRandom = SecureRandom()
    private val lock = Any()
    private val logger = InlineLogger()

    /**
     * Generate a random salt, this should be saved with the password hashed so that it can be used to validate
     */
    val salt: String
        get() {
            val salt = ByteArray(16)
            secureRandom.nextBytes(salt)
            return String(salt)
        }

    /**
     * Hash a string with sha-2
     *
     * @param password the pass, should be salted by now
     */
    fun hashSHA(password: String): String? {
        val md: MessageDigest?
        val sb = StringBuilder()
        try {
            md = MessageDigest.getInstance("SHA-256")
            md!!.update(password.toByteArray())
            val byteData = md.digest()
            for (aByteData in byteData) {
                sb.append(((aByteData.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
        } catch (e: NoSuchAlgorithmException) {
            logger.error { "Error hashing password!" }
            return null
        }

        return sb.toString()
    }

    /**
     * Used for comparing states, do not use this for password please.
     */
    fun encryptMD5(buffer: ByteArray): ByteArray? {
        synchronized(lock) {
            try {
                val algorithm = MessageDigest.getInstance("MD5")
                algorithm.update(buffer)
                val digest = algorithm.digest()
                algorithm.reset()
                return digest
            } catch (e: Throwable) {
                logger.error(e) { "MD5" }
            }

            return null
        }
    }
}