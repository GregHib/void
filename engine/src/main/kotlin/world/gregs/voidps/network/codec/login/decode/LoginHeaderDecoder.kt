package world.gregs.voidps.network.codec.login.decode

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.codec.login.LoginResponseCode
import world.gregs.voidps.utility.getIntProperty
import world.gregs.voidps.utility.getProperty
import java.math.BigInteger

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object LoginHeaderDecoder {

    private val logger = InlineLogger()
    private val clientMajorBuild: Int = getIntProperty("clientBuild")
    private val loginRSAModulus = BigInteger(getProperty("lsRsaModulus"), 16)
    private val loginRSAPrivate = BigInteger(getProperty("lsRsaPrivate"), 16)

    /**
     * Decodes login message
     * @param reader Packet to decode
     * @param extra Whether to read extra byte
     * @return Triple(password, server seed, client seed)
     */
    fun decode(reader: Reader): Triple<LoginResponseCode, String?, IntArray?> {
        val version = reader.readInt()
        if (version != clientMajorBuild) {
            return Triple(LoginResponseCode.GameUpdated, null, null)
        }

        val rsaBlockSize = reader.readUnsignedShort()//RSA block size
        if (rsaBlockSize > reader.readableBytes()) {
            logger.debug { "Received bad rsa block size [size=$rsaBlockSize, readable=${reader.readableBytes()}" }
            return Triple(LoginResponseCode.BadSessionId, null, null)
        }
        val data = ByteArray(rsaBlockSize)
        reader.readBytes(data)
        val rsa = RSA.crypt(data, loginRSAModulus, loginRSAPrivate)
        val rsaBuffer = BufferReader(rsa)
        val sessionId = rsaBuffer.readUnsignedByte()
        if (sessionId != 10) {//rsa block start check
            logger.debug { "Bad session id received ($sessionId)" }
            return Triple(LoginResponseCode.BadSessionId, null, null)
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsaBuffer.readInt()
        }

        val passBlock = rsaBuffer.readLong()
        if (passBlock != 0L) {//password should start here (marked by 0L)
            logger.info { "Rsa start marked by 0L was not true ($passBlock)" }
            return Triple(LoginResponseCode.BadSessionId, null, null)
        }

        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        Xtea.decipher((reader as BufferReader).buffer, isaacKeys)
        return Triple(LoginResponseCode.Successful, password, isaacKeys)
    }

}