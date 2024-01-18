package world.gregs.voidps.js5

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class Network(
    private val server: FileServer,
    private val prefetchKeys: IntArray,
    private val revision: Int,
    private val acknowledgeId: Int,
    private val statusId: Int,
) {
    private val logger = InlineLogger()

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        logger.warn(throwable) { "Network error $context" }
    }

    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false

    /**
     * Start the server and begin creating a new coroutine for every new connection accepted
     * @param threads a fixed number or 0 to dynamically allocate based on need
     */
    fun start(port: Int, threads: Int) = runBlocking {
        val executor = if (threads == 0) Executors.newCachedThreadPool() else Executors.newFixedThreadPool(threads)
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
    }

    suspend fun connect(socket: Socket) {
        val read = socket.openReadChannel()
        val write = socket.openWriteChannel(autoFlush = true)
        synchronise(read, write)
        if (acknowledge(read, write)) {
            logger.trace { "Client synchronisation complete: $socket" }
            readRequests(read, write)
        }
    }

    /**
     * If the client is up-to-date and in the correct state send it the [prefetchKeys] list so it knows what indices are available to request
     */
    suspend fun synchronise(read: ByteReadChannel, write: ByteWriteChannel) {
        val opcode = read.readByte().toInt()
        if (opcode != SYNCHRONISE) {
            logger.trace { "Invalid sync session id: $opcode" }
            write.writeByte(REJECT_SESSION)
            write.close()
            return
        }

        val revision = read.readInt()
        if (revision != this.revision) {
            logger.trace { "Invalid game revision: $revision" }
            write.writeByte(GAME_UPDATED)
            write.close()
            return
        }

        write.writeByte(0)
        prefetchKeys.forEach { key ->
            write.writeInt(key)
        }
    }

    /**
     * Confirm the client got our message and is ready to start sending file requests
     */
    suspend fun acknowledge(read: ByteReadChannel, write: ByteWriteChannel): Boolean {
        val opcode = read.readByte().toInt()
        if (opcode != ACKNOWLEDGE) {
            logger.trace { "Invalid ack opcode: $opcode" }
            write.writeByte(REJECT_SESSION)
            write.close()
            return false
        }

        return verify(read, write, acknowledgeId)
    }

    /**
     * Confirm a session value send by the client is as the server [expected]
     */
    suspend fun verify(read: ByteReadChannel, write: ByteWriteChannel, expected: Int): Boolean {
        val id = read.readMedium()
        if (id != expected) {
            logger.trace { "Invalid session id expected: $expected actual: $id" }
            write.writeByte(BAD_SESSION_ID)
            write.close()
            return false
        }
        return true
    }

    suspend fun readRequests(read: ByteReadChannel, write: ByteWriteChannel) = coroutineScope {
        try {
            while (isActive) {
                readRequest(read, write)
            }
        } finally {
            logger.trace { "Client disconnected: $read" }
        }
    }

    /**
     * Verify status updates and pass requests onto the [server] to fulfill
     */
    suspend fun readRequest(read: ByteReadChannel, write: ByteWriteChannel) {
        when (val opcode = read.readByte().toInt()) {
            STATUS_LOGGED_OUT, STATUS_LOGGED_IN -> verify(read, write, statusId)
            PRIORITY_REQUEST, PREFETCH_REQUEST -> {
                server.fulfill(read, write, opcode == PREFETCH_REQUEST)
            }
            else -> {
                logger.warn { "Unknown request $opcode." }
                write.close()
            }
        }
    }

    fun stop() {
        running = false
        dispatcher.close()
    }

    companion object {
        // Opcodes
        const val PREFETCH_REQUEST = 0
        const val PRIORITY_REQUEST = 1
        const val SYNCHRONISE = 15
        const val STATUS_LOGGED_IN = 2
        const val STATUS_LOGGED_OUT = 3
        const val ACKNOWLEDGE = 6

        // Response codes
        private const val GAME_UPDATED = 6
        private const val BAD_SESSION_ID = 10
        private const val REJECT_SESSION = 11
    }
}