package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Decoder.Companion.BYTE
import world.gregs.voidps.network.Decoder.Companion.SHORT
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network(
    private val protocol: Map<Int, Decoder>,
    private val revision: Int,
    private val modulus: BigInteger,
    private val private: BigInteger,
    private val loginQueue: LoginQueue,
    private val factory: PlayerFactory,
    private val gameContext: CoroutineDispatcher,
    private val loginLimit: Int
) {

    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false

    fun start(port: Int) = runBlocking {
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.warn { throwable.message }
        }
        val scope = CoroutineScope(coroutineContext + supervisor + exceptionHandler)
        with(scope) {
            val server = aSocket(selector).tcp().bind(port = port)
            running = true
            logger.info { "Listening for requests on port ${port}..." }
            while (running) {
                val socket = server.accept()
                logger.trace { "New connection accepted ${socket.remoteAddress}" }
                val read = socket.openReadChannel()
                val write = socket.openWriteChannel(autoFlush = false)
                launch(Dispatchers.IO) {
                    connect(read, write, socket.remoteAddress.hostname)
                }
            }
        }
        running = true
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        synchronise(read, write)
        login(read, write, hostname)
    }

    suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != SYNCHRONISE) {
            logger.trace { "Invalid sync session id: $opcode" }
            write.finish(LOGIN_SERVER_REJECTED_SESSION)
            return
        }
        write.respond(ACCEPT_SESSION)
    }

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        val opcode = read.readByte().toInt()
        if (opcode != LOGIN && opcode != RECONNECT) {
            logger.trace { "Invalid request id: $opcode" }
            write.finish(LOGIN_SERVER_REJECTED_SESSION)
            return
        }
        val size = read.readShort().toInt()
        val packet = read.readPacket(size)
        checkClientVersion(read, packet, write, hostname)
    }

    suspend fun checkClientVersion(read: ByteReadChannel, packet: ByteReadPacket, write: ByteWriteChannel, hostname: String) {
        val version = packet.readInt()
        if (version != revision) {
            logger.trace { "Invalid revision: $version" }
            write.finish(GAME_UPDATE)
            return
        }
        val rsa = decryptRSA(packet)
        validateSession(read, rsa, packet, write, hostname)
    }

    fun decryptRSA(packet: ByteReadPacket): ByteReadPacket {
        val rsaBlockSize = packet.readUShort().toInt()
        val data = packet.readBytes(rsaBlockSize)
        val rsa = RSA.crypt(data, modulus, private)
        return ByteReadPacket(rsa)
    }

    suspend fun validateSession(read: ByteReadChannel, rsa: ByteReadPacket, packet: ByteReadPacket, write: ByteWriteChannel, hostname: String) {
        val sessionId = rsa.readUByte().toInt()
        if (sessionId != SESSION) {
            logger.debug { "Bad session id $sessionId" }
            write.finish(BAD_SESSION_ID)
            return
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsa.readInt()
        }

        val passwordMarker = rsa.readLong()
        if (passwordMarker != 0L) {
            logger.info { "Incorrect password marker $passwordMarker" }
            write.finish(BAD_SESSION_ID)
            return
        }
        val password: String = rsa.readString()
        val xtea = decryptXtea(packet, isaacKeys)

        val username = xtea.readString()
        xtea.readUByte()// social login
        val displayMode = xtea.readUByte().toInt()
        val client = createClient(write, isaacKeys, hostname)
        login(read, write, client, username, password, displayMode)
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

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel, client: Client, username: String, password: String, displayMode: Int) {
        if (loginQueue.isOnline(username)) {
            write.finish(ACCOUNT_ONLINE)
            return
        }

        if (loginQueue.logins(client.address) >= loginLimit) {
            write.finish(LOGIN_LIMIT_EXCEEDED)
            return
        }

        val index = loginQueue.login(username, client.address)
        if (index == null) {
            loginQueue.logout(username, client.address, index)
            write.finish(WORLD_FULL)
            return
        }

        val player = loadPlayer(write, client, username, password, index) ?: return
        write.sendLoginDetails(username, index, 2)
        withContext(gameContext) {
            player.gameFrame.displayMode = displayMode
            logger.info { "Player logged in $username index $index." }
            player.login(client)
        }
        try {
            readPackets(client, player, read)
        } finally {
            client.exit()
        }
    }

    private suspend fun ByteWriteChannel.sendLoginDetails(username: String, index: Int, rights: Int) {
        writeByte(SUCCESS)
        writeByte(13 + string(username))
        writeByte(rights)
        writeByte(0)// Unknown - something to do with skipping chat messages
        writeByte(0)
        writeByte(0)
        writeByte(0)
        writeByte(0)// Moves chat box position
        writeShort(index)
        writeByte(true)
        writeMedium(0)
        writeByte(true)
        writeString(username)
        flush()
    }

    suspend fun loadPlayer(write: ByteWriteChannel, client: Client, username: String, password: String, index: Int): Player? {
        try {
            var account = factory.load(username)
            if (account == null) {
                account = factory.create(username, password)
            } else if (account.passwordHash.isBlank() || !BCrypt.checkpw(password, account.passwordHash)) {
                loginQueue.logout(username, client.address, index)
                write.finish(INVALID_CREDENTIALS)
                return null
            }
            factory.initPlayer(account, index)
            logger.info { "Player $username loaded and queued for login." }
            loginQueue.await()
            return account
        } catch (e: IllegalStateException) {
            loginQueue.logout(username, client.address, index)
            write.finish(COULD_NOT_COMPLETE_LOGIN)
            return null
        }
    }

    suspend fun readPackets(client: Client, player: Player, read: ByteReadChannel) {
        while (true) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val decoder = protocol[opcode]
            if (decoder == null) {
                logger.error { "No decoder for message opcode $opcode" }
                return
            }
            val size = when (decoder.length) {
                BYTE -> read.readUByte()
                SHORT -> read.readUShort()
                else -> decoder.length
            }
            val packet = read.readPacket(size = size)
            decoder.decode(player.instructions, packet)
        }
    }

    fun shutdown() {
        running = false
        dispatcher.close()
    }

    private suspend fun ByteReadChannel.readUByte(): Int = readByte().toInt() and 0xff
    private suspend fun ByteReadChannel.readUShort(): Int = (readUByte() shl 8) or readUByte()

    private suspend fun ByteWriteChannel.respond(value: Int) {
        writeByte(value)
        flush()
    }

    private suspend fun ByteWriteChannel.finish(value: Int) {
        respond(value)
        close()
    }

    companion object {
        private val logger = InlineLogger()

        private const val SYNCHRONISE = 14
        private const val LOGIN = 16
        private const val RECONNECT = 18
        private const val SESSION = 10

        private const val ACCEPT_SESSION = 0

        // Login responses
        private const val DATA_CHANGE = 0
        private const val VIDEO_AD = 1
        private const val SUCCESS = 2
        private const val INVALID_CREDENTIALS = 3
        private const val ACCOUNT_DISABLED = 4
        private const val ACCOUNT_ONLINE = 5
        private const val GAME_UPDATE = 6
        private const val WORLD_FULL = 7
        private const val LOGIN_SERVER_OFFLINE = 8
        private const val LOGIN_LIMIT_EXCEEDED = 9
        private const val BAD_SESSION_ID = 10
        private const val LOGIN_SERVER_REJECTED_SESSION = 11
        private const val MEMBERS_ACCOUNT_REQUIRED = 12
        private const val COULD_NOT_COMPLETE_LOGIN = 13
        private const val SERVER_BEING_UPDATED = 14
        private const val RECONNECTING = 15
        private const val LOGIN_ATTEMPTS_EXCEEDED = 16
        private const val MEMBERS_ONLY_AREA = 17
        private const val INVALID_LOGIN_SERVER = 20
        private const val TRANSFERRING_PROFILE = 21
    }
}