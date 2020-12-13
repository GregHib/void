package rs.dusk.network.rs.codec.login.decode

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.secure.RSA
import rs.dusk.cache.secure.Xtea
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.LoginResponseCode
import rs.dusk.utility.getIntProperty
import rs.dusk.utility.getProperty
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
    fun decode(reader: PacketReader, extra: Boolean = false): Triple<LoginResponseCode, String?, IntArray?> {
        val version = reader.readInt()
        if (version != clientMajorBuild) {
            return Triple(LoginResponseCode.GameUpdated, null, null)
        }

        if (extra) {
            reader.readUnsignedByte()
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
        println("isaacKeys=${isaacKeys.contentToString()}")

        val passBlock = rsaBuffer.readLong()
        if (passBlock != 0L) {//password should start here (marked by 0L)
            logger.info { "Rsa start marked by 0L was not true ($passBlock)" }
            return Triple(LoginResponseCode.BadSessionId, null, null)
        }

        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        Xtea.decipher(reader.buffer, isaacKeys)
        return Triple(LoginResponseCode.Successful, password, isaacKeys)
    }

}