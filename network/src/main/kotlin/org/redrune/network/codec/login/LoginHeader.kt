package org.redrune.network.codec.login

import com.github.michaelbull.logging.InlineLogger
import org.redrune.cache.secure.RSA
import org.redrune.cache.secure.Xtea
import org.redrune.network.model.packet.PacketReader
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object LoginHeader {

    private val logger = InlineLogger()

    /**
     * Decodes login message
     * @param reader Packet to decode
     * @param extra Whether to read extra byte
     * @return Triple(password, server seed, client seed)
     */
    fun decode(reader: PacketReader, extra: Boolean = false): Triple<LoginResponseCode, String?, IntArray?> {
        val version = reader.readInt()
        if (version != NetworkConstants.CLIENT_MAJOR_BUILD) {
            return Triple(LoginResponseCode.UPDATED, null, null)
        }

        if (extra) {
            reader.readUnsignedByte()
        }

        val rsaBlockSize = reader.readUnsignedShort()//RSA block size
        if (rsaBlockSize > reader.readableBytes()) {
            logger.warn { "Received bad rsa block size [size=$rsaBlockSize, readable=${reader.readableBytes()}" }
            return Triple(LoginResponseCode.BAD_SESSION_ID, null, null)
        }
        val data = ByteArray(rsaBlockSize)
        reader.readBytes(data)
        val rsa = RSA.crypt(data, NetworkConstants.LOGIN_RSA_MODULUS, NetworkConstants.LOGIN_RSA_PRIVATE)
        val rsaBuffer = PacketReader(rsa)
        val sessionId = rsaBuffer.readUnsignedByte()
        if (sessionId != 10) {//rsa block start check
            logger.warn { "Bad session id received ($sessionId)" }
            return Triple(LoginResponseCode.BAD_SESSION_ID, null, null)
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsaBuffer.readInt()
        }

        val passBlock = rsaBuffer.readLong()
        if (passBlock != 0L) {//password should start here (marked by 0L)
            logger.info { "Rsa start marked by 0L was not true ($passBlock)"}
            return Triple(LoginResponseCode.BAD_SESSION_ID, null, null)
        }

        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        Xtea.decipher(reader.payload, isaacKeys)
        return Triple(LoginResponseCode.SUCCESSFUL, password, isaacKeys)
    }

}