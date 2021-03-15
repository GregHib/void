package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.getIntProperty
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network(
    private val codec: NetworkCodec
) {

    private val logger = InlineLogger()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.warn { throwable.message }
    }
    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false
    private val loginQueue: LoginQueue by inject()
    private val loader: PlayerLoader by inject()

    fun start(port: Int) = runBlocking {
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val scope = CoroutineScope(coroutineContext + supervisor + exceptionHandler)
        with(scope) {
            val server = aSocket(selector).tcp().bind(port = port)
            running = true
            logger.info { "Listening for requests on port ${port}..." }
            while (running) {
                val socket = server.accept()
                logger.trace { "New connection accepted $socket" }
                val read = socket.openReadChannel()
                val write = socket.openWriteChannel(autoFlush = false)
                launch(Dispatchers.IO) {
                    connect(read, write)
                }
            }
        }
        running = true
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel) {
        synchronise(read, write)
        login(read, write)
    }

    suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != SYNCHRONISE) {
            logger.trace { "Invalid sync session id: $opcode" }
            write.writeByte(REJECT_SESSION)
            write.flush()
            write.close()
            return
        }

        write.writeByte(ACCEPT_SESSION)
        write.flush()
    }

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != LOGIN && opcode != RECONNECT) {
            logger.trace { "Invalid request id: $opcode" }
            write.writeByte(REJECT_SESSION)
            write.flush()
            write.close()
            return
        }
        val size = read.readShort().toInt()
        val packet = read.readPacket(size)

        read(read, packet, write)
    }

    suspend fun read(read: ByteReadChannel, packet: ByteReadPacket, write: ByteWriteChannel) {
        val version = packet.readInt()
        if (version != 634) {
            write.writeByte(GameUpdate)
            write.flush()
            write.close()
            return
        }

        val rsaBlockSize = packet.readShort().toInt() and 0xffff
        if (rsaBlockSize > packet.remaining) {
            logger.debug { "Received bad rsa block size [size=$rsaBlockSize, readable=${packet.remaining}" }
            write.writeByte(BadSessionId)
            write.flush()
            write.close()
            return
        }
        val data = ByteArray(rsaBlockSize)
        packet.readFully(data)
        val rsa = RSA.crypt(data, loginRSAModulus, loginRSAPrivate)
        val rsaBuffer = BufferReader(rsa)
        val sessionId = rsaBuffer.readUnsignedByte()
        if (sessionId != 10) {//rsa block start check
            logger.debug { "Bad session id received ($sessionId)" }
            write.writeByte(BadSessionId)
            write.flush()
            write.close()
            return
        }

        val isaacKeys = IntArray(4)
        for (i in isaacKeys.indices) {
            isaacKeys[i] = rsaBuffer.readInt()
        }

        val passBlock = rsaBuffer.readLong()
        if (passBlock != 0L) {//password should start here (marked by 0L)
            logger.info { "Rsa start marked by 0L was not true ($passBlock)" }
            write.writeByte(BadSessionId)
            write.flush()
            write.close()
            return
        }
        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        val remaining = ByteArray(packet.remaining.toInt())
        packet.readFully(remaining)
        Xtea.decipher(remaining, isaacKeys)

        val inCipher = IsaacCipher(isaacKeys)
        for (i in isaacKeys.indices) {
            isaacKeys[i] += 50
        }
        val outCipher = IsaacCipher(isaacKeys)
        val login = ByteReadPacket(remaining)
        val username = login.readString()
        login.readUByte()// social login
        val displayMode = login.readUByte().toInt()
        val screenWidth = login.readUShort().toInt()
        val screenHeight = login.readUShort().toInt()

        val client = Client(write, inCipher, outCipher)
        connect(read, write, client, username, password, 0, 0, displayMode)
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, client: Client, username: String, password: String, screenWidth: Int, screenHeight: Int, displayMode: Int) {
        if (loginQueue.isOnline(username)) {
            write.writeByte(AccountOnline)
            write.flush()
            write.close()
            return
        }
        val index = loginQueue.login(username)
        if (index == null) {
            loginQueue.logout(username, index)
            write.writeByte(WorldFull)
            write.flush()
            write.close()
            return
        }
        val player = loadPlayer(write, username, password, index) ?: return
        write.apply {
            writeByte(Success)
            val rights = 2
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
        withContext(Contexts.Game) {
            player.gameFrame.width = screenWidth
            player.gameFrame.height = screenHeight
            player.gameFrame.displayMode = displayMode
            logger.info { "Player spawned $username index $index." }
            player.login(client)
        }

        try {
            readPackets(client, read, player)
        } finally {
            client.exit()
        }
    }

    suspend fun loadPlayer(write: ByteWriteChannel, username: String, password: String, index: Int): Player? {
        try {
            var account = loader.load(username)
            if (account == null) {
                account = createNewAccount(username, password)
            } else if (account.passwordHash.isBlank() || !BCrypt.checkpw(password, account.passwordHash)) {
                loginQueue.logout(username, index)
                write.writeByte(InvalidCredentials)
                write.flush()
                write.close()
                return null
            }
            loader.initPlayer(account, index)
            logger.info { "Player $username loaded and queued for login." }
            loginQueue.await()
            return account
        } catch (e: IllegalStateException) {
            loginQueue.logout(username, index)
            write.writeByte(CouldNotCompleteLogin)
            write.flush()
            write.close()
            return null
        }
    }

    fun createNewAccount(username: String, password: String): Player {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val x = getIntProperty("homeX", 0)
        val y = getIntProperty("homeY", 0)
        val plane = getIntProperty("homePlane", 0)
        val tile = Tile(x, y, plane)
        return Player(id = -1, tile = tile, name = username, passwordHash = hash)
    }

    suspend fun readPackets(client: Client, read: ByteReadChannel, player: Player) {
        while (true) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val decoder = codec.getDecoder(opcode)
            if (decoder == null) {
                logger.error { "Unable to identify length of packet $opcode" }
                return
            }
            val size = when (decoder.length) {
                -1 -> read.readUByte()
                -2 -> (read.readUByte() shl 8) or read.readUByte()
                else -> decoder.length
            }

            val packet = read.readPacket(size = size)
            decoder.decode(player, BufferReader(packet.readBytes()))
        }
    }

    private suspend fun ByteReadChannel.readUByte() = readByte().toInt() and 0xff

    private val loginRSAModulus = BigInteger(getProperty("lsRsaModulus"), 16)
    private val loginRSAPrivate = BigInteger(getProperty("lsRsaPrivate"), 16)

    fun shutdown() {
        running = false
        dispatcher.close()
    }

    companion object {
        private const val SYNCHRONISE = 14
        private const val LOGIN = 16
        private const val RECONNECT = 18

        private const val ACCEPT_SESSION = 0
        private const val REJECT_SESSION = 0// Wrong?


        // Login responses
        private const val DataChange = 0
        private const val VideoAd = 1
        private const val Success = 2
        private const val InvalidCredentials = 3
        private const val AccountDisabled = 4
        private const val AccountOnline = 5
        private const val GameUpdate = 6
        private const val WorldFull = 7
        private const val LoginServerOffline = 8
        private const val LoginLimitExceeded = 9
        private const val BadSessionId = 10
        private const val LoginServerRejectedSession = 11
        private const val MembersAccountRequired = 12
        private const val CouldNotCompleteLogin = 13
        private const val ServerBeingUpdated = 14
        private const val Reconnecting = 15
        private const val LoginAttemptsExceeded = 16
        private const val MembersOnlyArea = 17
        private const val InvalidLoginServer = 20
        private const val TransferringProfile = 21
    }
}