package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.IsaacCipher
import java.math.BigInteger
import java.util.*

/**
 * Connects a client to their account in the game world
 */
@ExperimentalUnsignedTypes
class LoginServer(
    private val protocol: Array<Decoder?>,
    private val revision: Int,
    private val modulus: BigInteger,
    private val private: BigInteger,
    private val gatekeeper: NetworkGatekeeper,
    private val loader: AccountLoader
) : Server {

    override suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        write.respond(Response.DATA_CHANGE)
        val opcode = read.readByte().toInt()
        if (opcode != Request.LOGIN && opcode != Request.RECONNECT) {
            logger.trace { "Invalid request id: $opcode" }
            write.finish(Response.LOGIN_SERVER_REJECTED_SESSION)
            return
        }
        val size = read.readShort().toInt()
        val packet = read.readPacket(size)
        checkClientVersion(read, packet, write, hostname)
    }

    private suspend fun checkClientVersion(read: ByteReadChannel, packet: ByteReadPacket, write: ByteWriteChannel, hostname: String) {
        val version = packet.readInt()
        if (version != revision) {
            logger.trace { "Invalid client revision: $version" }
            write.finish(Response.GAME_UPDATE)
            return
        }
        val rsa = decryptRSA(packet)
        validateSession(read, rsa, packet, write, hostname)
    }

    private fun decryptRSA(packet: ByteReadPacket): ByteReadPacket {
        val rsaBlockSize = packet.readUShort().toInt()
        val data = packet.readBytes(rsaBlockSize)
        val rsa = RSA.crypt(data, modulus, private)
        return ByteReadPacket(rsa)
    }

    suspend fun validateSession(read: ByteReadChannel, rsa: ByteReadPacket, packet: ByteReadPacket, write: ByteWriteChannel, hostname: String) {
        val sessionId = rsa.readUByte().toInt()
        if (sessionId != Request.SESSION) {
            logger.debug { "Bad session id $sessionId" }
            write.finish(Response.BAD_SESSION_ID)
            return
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsa.readInt()
        }

        val passwordMarker = rsa.readLong()
        if (passwordMarker != 0L) {
            logger.info { "Incorrect password marker $passwordMarker" }
            write.finish(Response.BAD_SESSION_ID)
            return
        }
        val password: String = rsa.readString()
        val xtea = decryptXtea(packet, isaacKeys)

        val username = xtea.readString()
        if (gatekeeper.connected(username)) {
            write.finish(Response.ACCOUNT_ONLINE)
            return
        }

        xtea.readUByte()// social login
        val displayMode = xtea.readUByte().toInt()
        val client = createClient(write, isaacKeys, hostname)
        login(read, client, username, password, displayMode)
    }

    private fun createClient(write: ByteWriteChannel, isaacKeys: IntArray, hostname: String): Client {
        val inCipher = IsaacCipher(isaacKeys)
        for (i in isaacKeys.indices) {
            isaacKeys[i] += 50
        }
        val outCipher = IsaacCipher(isaacKeys)
        return Client(write, inCipher, outCipher, hostname)
    }

    private fun decryptXtea(packet: ByteReadPacket, isaacKeys: IntArray): ByteReadPacket {
        val remaining = packet.readBytes(packet.remaining.toInt())
        Xtea.decipher(remaining, isaacKeys)
        return ByteReadPacket(remaining)
    }

    suspend fun login(read: ByteReadChannel, client: Client, username: String, password: String, displayMode: Int) {
        val index = gatekeeper.connect(username, client.address)
        client.onDisconnected {
            gatekeeper.disconnect(username, client.address)
        }
        if (index == null) {
            client.disconnect(Response.WORLD_FULL)
            return
        }
        val instructions = loader.load(client, username, password, index, displayMode) ?: return
        try {
            readPackets(client, instructions, read)
        } finally {
            client.exit()
        }
    }

    private suspend fun readPackets(client: Client, instructions: MutableSharedFlow<Instruction>, read: ByteReadChannel) {
        while (!client.disconnected) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val decoder = protocol[opcode]
            if (decoder == null) {
                logger.error { "No decoder for message opcode $opcode" }
                return
            }
            val size = when (decoder.length) {
                Decoder.BYTE -> read.readUByte()
                Decoder.SHORT -> read.readUShort()
                else -> decoder.length
            }
            val packet = read.readPacket(size = size)
            decoder.decode(instructions, packet)
        }
    }
    
    companion object {
        private val logger = InlineLogger()

        fun load(properties: Properties, protocol: Array<Decoder?>, gatekeeper: NetworkGatekeeper, loader: AccountLoader): LoginServer {
            val gameModulus = BigInteger(properties.getProperty("gameModulus"), 16)
            val gamePrivate = BigInteger(properties.getProperty("gamePrivate"), 16)
            val revision = properties.getProperty("revision").toInt()
            return LoginServer(protocol, revision, gameModulus, gamePrivate, gatekeeper, loader)
        }
    }
}