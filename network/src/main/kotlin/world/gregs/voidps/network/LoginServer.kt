package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.IsaacCipher
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.PasswordManager
import world.gregs.voidps.network.login.protocol.*
import java.math.BigInteger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Connects a client to their account in the game world
 */
class LoginServer(
    private val protocol: Array<Decoder?>,
    private val revision: Int,
    private val loginLimit: Int,
    private val modulus: BigInteger,
    private val private: BigInteger,
    private val accounts: AccountLoader,
    private val passwordManager: PasswordManager = PasswordManager(accounts),
) : Server {

    internal val online: MutableSet<String> = ConcurrentHashMap.newKeySet()

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

    private suspend fun checkClientVersion(read: ByteReadChannel, packet: Source, write: ByteWriteChannel, hostname: String) {
        val version = packet.readInt()
        if (version != revision) {
            logger.trace { "Invalid client revision: $version" }
            write.finish(Response.GAME_UPDATE)
            return
        }
        val rsaBlockSize = packet.readUShort().toInt()
        if (rsaBlockSize == 0) {
            logger.debug { "Invalid rsa block size." }
            write.finish(Response.COULD_NOT_COMPLETE_LOGIN)
            return
        }
        val data = packet.readByteArray(rsaBlockSize)
        val rsa = RSA.crypt(data, modulus, private)
        validateSession(read, ByteReadPacket(rsa), packet, write, hostname)
    }

    private suspend fun validateSession(read: ByteReadChannel, rsa: Source, packet: Source, write: ByteWriteChannel, hostname: String) {
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
        if (!validate(write, username, password)) {
            return
        }
        val client = createClient(write, isaacKeys, hostname)
        client.onDisconnected {
            online.remove(username)
        }
        val passwordHash = passwordManager.encrypt(username, password)
        xtea.readUByte() // social login
        val displayMode = xtea.readUByte().toInt()
        login(read, client, username, passwordHash, displayMode)
    }

    suspend fun validate(write: ByteWriteChannel, username: String, password: String): Boolean {
        val response = passwordManager.validate(username, password)
        if (response != Response.SUCCESS) {
            write.finish(response)
            return false
        }
        if (username.length > 12) {
            write.finish(Response.INVALID_CREDENTIALS)
            return false
        }
        if (!online.add(username)) {
            write.finish(Response.ACCOUNT_ONLINE)
            return false
        }
        if (online.size >= loginLimit) {
            write.finish(Response.WORLD_FULL)
            return false
        }
        return true
    }

    private fun createClient(write: ByteWriteChannel, isaacKeys: IntArray, hostname: String): Client {
        val inCipher = IsaacCipher(isaacKeys)
        for (i in isaacKeys.indices) {
            isaacKeys[i] += 50
        }
        val outCipher = IsaacCipher(isaacKeys)
        return Client(write, inCipher, outCipher, hostname)
    }

    private fun decryptXtea(packet: Source, isaacKeys: IntArray): Source {
        val remaining = packet.readByteArray(packet.remaining.toInt())
        Xtea.decipher(remaining, isaacKeys)
        return ByteReadPacket(remaining)
    }

    suspend fun login(read: ByteReadChannel, client: Client, username: String, passwordHash: String, displayMode: Int) {
        try {
            val instructions = accounts.load(client, username, passwordHash, displayMode) ?: return
            readPackets(client, instructions, read)
        } finally {
            client.exit()
            client.disconnect()
        }
    }

    private suspend fun readPackets(client: Client, instructions: SendChannel<Instruction>, read: ByteReadChannel) {
        while (!client.disconnected) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff // Exhausted due to client termination. Something not written correctly I would guess.
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
            val packet = read.readPacket(size)
            instructions.send(decoder.decode(packet) ?: continue)
        }
    }

    companion object {
        private val logger = InlineLogger()

        fun load(properties: Properties, protocol: Array<Decoder?>, loader: AccountLoader): LoginServer {
            val gameModulus = BigInteger(properties.getProperty("security.game.modulus"), 16)
            val gamePrivate = BigInteger(properties.getProperty("security.game.private"), 16)
            val revision = properties.getProperty("server.revision").toInt()
            val maxPlayers = properties.getProperty("world.players.max").toInt()
            return LoginServer(protocol, revision, maxPlayers, gameModulus, gamePrivate, loader)
        }
    }
}
