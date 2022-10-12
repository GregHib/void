package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.Decoder.Companion.BYTE
import world.gregs.voidps.network.Decoder.Companion.SHORT
import java.math.BigInteger
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
class Network(
    private val revision: Int,
    private val modulus: BigInteger,
    private val private: BigInteger,
    private val gatekeeper: NetworkGatekeeper,
    private val loader: AccountLoader,
    private val loginLimit: Int,
    private val disconnectContext: CoroutineDispatcher,
    private val protocol: Map<Int, Decoder>
) {

    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false

    fun start(port: Int) = runBlocking {
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "Connection error" }
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
                    connect(read, write, socket.remoteAddress.toJavaAddress().hostname)
                }
            }
        }
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        if (gatekeeper.connections(hostname) >= loginLimit) {
            write.finish(Response.LOGIN_LIMIT_EXCEEDED)
            return
        }
        synchronise(read, write)
        login(read, write, hostname)
    }

    private suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != SYNCHRONISE) {
            logger.trace { "Invalid sync session id: $opcode" }
            write.finish(Response.LOGIN_SERVER_REJECTED_SESSION)
            return
        }
        write.respond(ACCEPT_SESSION)
    }

    private suspend fun login(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        val opcode = read.readByte().toInt()
        if (opcode != LOGIN && opcode != RECONNECT) {
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
            logger.trace { "Invalid revision: $version" }
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
        if (sessionId != SESSION) {
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
        client.on(disconnectContext, ClientState.Disconnected) {
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
            decoder.decode(instructions, packet)
        }
    }

    fun shutdown() {
        running = false
        dispatcher.close()
    }

    private suspend fun ByteReadChannel.readUByte(): Int = readByte().toInt() and 0xff
    private suspend fun ByteReadChannel.readUShort(): Int = (readUByte() shl 8) or readUByte()

    companion object {

        private suspend fun ByteWriteChannel.respond(value: Int) {
            writeByte(value)
            flush()
        }

        private suspend fun ByteWriteChannel.finish(value: Int) {
            respond(value)
            close()
        }

        private val logger = InlineLogger()

        private const val SYNCHRONISE = 14
        private const val LOGIN = 16
        private const val RECONNECT = 18
        private const val SESSION = 10

        private const val ACCEPT_SESSION = 0
    }
}