package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.writeByte
import world.gregs.voidps.buffer.write.writeMedium
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network {

    private val logger = InlineLogger()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable.message?.startsWith("EOF") == true) {
            return@CoroutineExceptionHandler
        }
        logger.warn { throwable.message }
    }
    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false
    private val loginQueue: LoginQueue by inject()
    private val loader: PlayerLoader by inject()

    private val game: GameCodec by inject()

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
                val write = socket.openWriteChannel(autoFlush = true)
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
            write.close()
            return
        }

        write.writeByte(ACCEPT_SESSION)
    }

    suspend fun login(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != LOGIN && opcode != RECONNECT) {
            logger.trace { "Invalid request id: $opcode" }
            write.writeByte(REJECT_SESSION)
            write.close()
            return
        }
        val size = read.readShort().toInt()
        val packet = read.readPacket(size)

        read(read, packet, write)
    }

    suspend fun read(read: ByteReadChannel, packet: ByteReadPacket, write: ByteWriteChannel) {
        var packet = packet
        val version = packet.readInt()
        if (version != 634) {
            write.writeByte(GameUpdate)
            write.close()
            return
        }

        val rsaBlockSize = packet.readShort().toInt() and 0xffff
        if (rsaBlockSize > packet.remaining) {
            logger.debug { "Received bad rsa block size [size=$rsaBlockSize, readable=${packet.remaining}" }
            write.writeByte(BadSessionId)
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
        packet = ByteReadPacket(remaining)
        val username = packet.readString()
        packet.readUByte()// social login
        val displayMode = packet.readUByte().toInt()
        val screenWidth = packet.readUShort().toInt()
        val screenHeight = packet.readUShort().toInt()
        val antialiasLevel = packet.readUByte().toInt()
        packet.skip(24)// graphics preferences
        val settings = packet.readString()
        val affiliateId = packet.readInt()
        packet.skip(packet.readUByte().toInt())// useless settings
        val sessionId2 = packet.readUByte().toInt()

        val os = packet.readUByte().toInt()
        val is64Bit = packet.readUByte().toInt()
        val versionType = packet.readUByte().toInt()
        val vendorType = packet.readUByte().toInt()
        val javaRelease = packet.readUByte().toInt()
        val javaVersion = packet.readUByte().toInt()
        val javaUpdate = packet.readUByte().toInt()
        val isUnsigned = packet.readUByte().toInt()
        val heapSize = packet.readShort().toInt()
        val processorCount = packet.readUByte().toInt()
        val totalMemory = packet.readUMedium()
        packet.readShort()
        packet.readUByte()
        packet.readUByte()
        packet.readUByte()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readUByte()
        packet.readShort()
        val unknown3 = packet.readInt()
        val userFlow = packet.readLong()
        val hasAdditionalInformation = packet.readUByte().toInt() == 0
        if (hasAdditionalInformation) {
            val additionalInformation = packet.readString()
        }
        val hasJagtheora = packet.readUByte().toInt() == 0
        val js = packet.readUByte().toInt() == 0
        val hc = packet.readUByte().toInt() == 0

        val client = Client(write, inCipher, outCipher)
        // TODO on disconnect add to logout queue

        if (loginQueue.isOnline(username)) {
            write.writeByte(AccountOnline)
            write.close()
            return
        }

        val index = loginQueue.login(username)
        if (index == null) {
            write.writeByte(WorldFull)
            loginQueue.logout(username, index)
            write.close()
            return
        }
        val player = try {
            val account = loader.loadPlayer(username, index, client)
            logger.info { "Player $username loaded and queued for login." }
            loginQueue.await()
            account
        } catch (e: IllegalStateException) {
            write.writeByte(CouldNotCompleteLogin)
            loginQueue.logout(username, index)
            write.close()
            return
        }

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
        }
        withContext(Contexts.Game) {
            player.gameFrame.width = screenWidth
            player.gameFrame.height = screenHeight
            player.gameFrame.displayMode = displayMode
            logger.info { "Player spawned $username index $index." }
            player.login()
        }
        readPackets(client, read, player)
    }

    suspend fun readPackets(client: Client, read: ByteReadChannel, player: Player) {
        while (true) {
            val cipher = client.cipherIn.nextInt()
            val opcode = (read.readUByte() - cipher) and 0xff
            val decoder = game.getDecoder(opcode)
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

    fun ByteReadPacket.readString(): String {
        val sb = StringBuilder()
        var b: Int
        while (remaining > 0) {
            b = readByte().toInt()
            if (b == 0) {
                break
            }
            sb.append(b.toChar())
        }
        return sb.toString()
//        readUTF8UntilDelimiter(0.toChar().toString())
    }

    fun ByteReadPacket.skip(count: Int) = readBytes(count)

    fun ByteReadPacket.readUMedium() = (readUByte().toInt() shl 16) or (readUByte().toInt() shl 8) or readUByte().toInt()

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