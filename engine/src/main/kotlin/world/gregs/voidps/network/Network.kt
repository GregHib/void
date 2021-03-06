package world.gregs.voidps.network


import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.GameLoginInfo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.Login
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.login.LoginResponse
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.entity.character.player.logout.LogoutQueue
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.codec.game.GameCodec
import world.gregs.voidps.network.codec.login.encode.GameLoginDetailsEncoder
import world.gregs.voidps.network.codec.login.encode.LoginResponseEncoder
import world.gregs.voidps.network.crypto.IsaacKeyPair
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network {

    private val logger = InlineLogger()
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.warn { "${throwable.message} $context" }
    }
    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false
    private val loginQueue: LoginQueue by inject()
    private val logoutQueue: LogoutQueue by inject()

    private val game: GameCodec by inject()
    private val bus: EventBus by inject()
    private val responseEncoder = LoginResponseEncoder()
    private val loginEncoder = GameLoginDetailsEncoder()

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
                launch(Dispatchers.IO) {
                    connect(socket)
                }
            }
        }
        running = true
    }

    suspend fun connect(socket: Socket) {
        val read = socket.openReadChannel()
        val write = socket.openWriteChannel(autoFlush = true)
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

        val session = read(packet, write) { player, session ->
            readPackets(session, read, player)
        }
    }

    suspend fun read(read: ByteReadPacket, write: ByteWriteChannel, start: suspend (Player, ClientSession) -> Unit) {
        var read = read
        val version = read.readInt()
        if (version != 634) {
            write.writeByte(UPDATED)
            write.close()
            return
        }

        val rsaBlockSize = read.readShort().toInt() and 0xffff
        if (rsaBlockSize > read.remaining) {
            logger.debug { "Received bad rsa block size [size=$rsaBlockSize, readable=${read.remaining}" }
            write.writeByte(BAD_SESSION)
            write.close()
            return
        }
        val data = ByteArray(rsaBlockSize)
        read.readFully(data)
        val rsa = RSA.crypt(data, loginRSAModulus, loginRSAPrivate)
        val rsaBuffer = BufferReader(rsa)
        val sessionId = rsaBuffer.readUnsignedByte()
        if (sessionId != 10) {//rsa block start check
            logger.debug { "Bad session id received ($sessionId)" }
            write.writeByte(BAD_SESSION)
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
            write.writeByte(BAD_SESSION)
            write.close()
            return
        }
        val password: String = rsaBuffer.readString()
        val serverSeed = rsaBuffer.readLong()
        val clientSeed = rsaBuffer.readLong()
        val remaining = ByteArray(read.remaining.toInt())
        read.readFully(remaining)
        Xtea.decipher(remaining, isaacKeys)

        val isaacPair = IsaacKeyPair(isaacKeys)
        read = ByteReadPacket(remaining)
        val username = read.readString()
        read.readUByte()// social login
        val displayMode = read.readUByte().toInt()
        val screenWidth = read.readUShort().toInt()
        val screenHeight = read.readUShort().toInt()
        val antialiasLevel = read.readUByte().toInt()
        read.skip(24)// graphics preferences
        val settings = read.readString()
        val affiliateId = read.readInt()
        read.skip(read.readUByte().toInt())// useless settings
        val sessionId2 = read.readUByte().toInt()

        val os = read.readUByte().toInt()
        val is64Bit = read.readUByte().toInt()
        val versionType = read.readUByte().toInt()
        val vendorType = read.readUByte().toInt()
        val javaRelease = read.readUByte().toInt()
        val javaVersion = read.readUByte().toInt()
        val javaUpdate = read.readUByte().toInt()
        val isUnsigned = read.readUByte().toInt()
        val heapSize = read.readShort().toInt()
        val processorCount = read.readUByte().toInt()
        val totalMemory = read.readUMedium()
        read.readShort()
        read.readUByte()
        read.readUByte()
        read.readUByte()
        read.readByte()
        read.readString()
        read.readByte()
        read.readString()
        read.readByte()
        read.readString()
        read.readByte()
        read.readString()
        read.readUByte()
        read.readShort()
        val unknown3 = read.readInt()
        val userFlow = read.readLong()
        val hasAdditionalInformation = read.readUByte().toInt() == 0
        if (hasAdditionalInformation) {
            val additionalInformation = read.readString()
        }
        val hasJagtheora = read.readUByte().toInt() == 0
        val js = read.readUByte().toInt() == 0
        val hc = read.readUByte().toInt() == 0
        write.writeByte(SUCCESS)

        // TODO pass isaac keys to session to use for encoding.

        val session = ClientSession(write, isaacPair.inCipher, isaacPair.outCipher)
        // TODO on disconnect add to logout queue

        val callback: (LoginResponse) -> Unit = { response ->
            println("Callback $response")
            if (response is LoginResponse.Success) {
                val player = response.player
                loginEncoder.encode(session, 2, player.index, username)
                bus.emit(RegionLogin(player))
                bus.emit(PlayerRegistered(player))
                player.setup()
                bus.emit(Registered(player))
                runBlocking {
                    coroutineScope {
                        launch {
                            start.invoke(player, session)
                        }
                    }
                }
            } else {
                responseEncoder.encode(session, response.code)
            }
        }
        sync {
            loginQueue.add(
                Login(
                    username,
                    session,
                    callback,
                    GameLoginInfo(username, password, isaacKeys, displayMode, screenWidth, screenHeight, antialiasLevel, settings, affiliateId, sessionId2, os, is64Bit, versionType, vendorType, javaRelease, javaVersion, javaUpdate, isUnsigned, heapSize, processorCount, totalMemory)
                )
            )
        }
    }

    suspend fun readPackets(session: ClientSession, read: ByteReadChannel, player: Player) {
        try {
            while (true) {
                val cipher = session.cipherIn.nextInt()
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
                decoder.decode(session, BufferReader(packet.readBytes()))
            }
        } finally {
            logoutQueue.add(player)
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

    suspend fun readMore(read: ByteReadPacket, write: ByteWriteChannel, keyPair: IsaacKeyPair) {
    }

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

        private const val UPDATED = 6
        private const val BAD_SESSION = 10
        private const val SUCCESS = 2

        private const val ACCEPT_SESSION = 0
        private const val REJECT_SESSION = 0// Wrong?
    }
}