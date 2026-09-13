package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.EOFException
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.network.login.AccountCreator
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.protocol.decryptXtea
import world.gregs.voidps.network.login.protocol.finish
import world.gregs.voidps.network.login.protocol.readString
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import java.math.BigInteger
import java.util.*

/**
 * Answers the login screen's "New user" requests; every request is a fresh connection with a single byte reply
 */
class RegistrationServer(
    private val revision: Int,
    private val modulus: BigInteger,
    private val private: BigInteger,
    private val creator: AccountCreator,
    private val limiter: RegistrationLimiter,
) {

    suspend fun connect(opcode: Int, read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        val response = try {
            handle(opcode, read, hostname)
        } catch (e: EOFException) {
            logger.debug(e) { "Malformed registration packet from $hostname" }
            RegistrationResponse.UNAVAILABLE
        } catch (e: IllegalArgumentException) {
            logger.debug(e) { "Malformed registration packet from $hostname" }
            RegistrationResponse.UNAVAILABLE
        }
        write.finish(response)
    }

    private suspend fun handle(opcode: Int, read: ByteReadChannel, hostname: String): Int {
        val size = read.readShort().toInt()
        val packet = read.readPacket(size)
        val version = packet.readUShort().toInt()
        if (version != revision) {
            logger.trace { "Invalid client revision: $version" }
            return RegistrationResponse.CLIENT_OUTDATED
        }
        val rsaBlockSize = packet.readUShort().toInt()
        if (rsaBlockSize == 0) {
            logger.debug { "Invalid rsa block size." }
            return RegistrationResponse.UNAVAILABLE
        }
        val rsa = ByteReadPacket(RSA.crypt(packet.readByteArray(rsaBlockSize), modulus, private))
        val sessionId = rsa.readUByte().toInt()
        if (sessionId != Request.SESSION) {
            logger.debug { "Bad session id $sessionId" }
            return RegistrationResponse.UNAVAILABLE
        }
        val keys = IntArray(4) { rsa.readInt() }
        val xtea = packet.decryptXtea(keys)
        return when (opcode) {
            Request.SIGN_UP -> check(xtea)
            Request.CREATE_ACCOUNT -> create(xtea, hostname)
            else -> RegistrationResponse.UNAVAILABLE
        }
    }

    private fun check(xtea: Source): Int {
        val email = xtea.readString().lowercase()
        val response = RegistrationValidator.email(email)
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        return creator.available(email)
    }

    private suspend fun create(xtea: Source, hostname: String): Int {
        val email = xtea.readString().lowercase()
        val affiliate = xtea.readUShort().toInt()
        val password = xtea.readString()
        xtea.readLong() // user flow
        val language = xtea.readUByte().toInt()
        xtea.readUByte() // game id
        xtea.readByteArray(UID_LENGTH)
        if (xtea.readUByte().toInt() == 1) {
            xtea.readString() // additional information
        }
        val age = xtea.readUByte().toInt()
        val optIn = xtea.readUByte().toInt() == 1
        var response = RegistrationValidator.email(email)
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        response = RegistrationValidator.password(password, email)
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        response = RegistrationValidator.age(age)
        if (response != RegistrationResponse.SUCCESS) {
            return response
        }
        // Only well-formed requests count towards the limit so typos don't lock a player out
        if (!limiter.allow(hostname)) {
            logger.info { "Registration limit exceeded for $hostname" }
            return RegistrationResponse.BUSY
        }
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        return creator.create(Registration(email, passwordHash, hostname, language, affiliate, optIn))
    }

    companion object {
        private const val UID_LENGTH = 24
        private val logger = InlineLogger()

        fun load(properties: Properties, creator: AccountCreator): RegistrationServer {
            val gameModulus = BigInteger(properties.getProperty("security.game.modulus"), 16)
            val gamePrivate = BigInteger(properties.getProperty("security.game.private"), 16)
            val revision = properties.getProperty("server.revision").toInt()
            val limit = properties.getProperty("accounts.registration.maxPerIP", "3").toInt()
            val minutes = properties.getProperty("accounts.registration.windowMinutes", "60").toLong()
            val limiter = RegistrationLimiter(limit, minutes * 60_000L)
            return RegistrationServer(revision, gameModulus, gamePrivate, creator, limiter)
        }
    }
}
