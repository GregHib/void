package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.client.ConnectionTracker
import world.gregs.voidps.network.login.protocol.finish
import java.net.SocketException
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * A network server for client's to connect to the game with
 */
class GameServer(
    private val fileServer: Server,
    private val connections: ConnectionTracker,
) {
    private var job: Job? = null
    private var dispatcher: ExecutorCoroutineDispatcher? = null
    private var server: ServerSocket? = null
    var loginServer: Server? = null

    fun start(port: Int): Job {
        Runtime.getRuntime().addShutdownHook(thread(start = false) { stop() })
        val executor = Executors.newCachedThreadPool()
        val dispatcher = executor.asCoroutineDispatcher()
        this.dispatcher = dispatcher
        try {
            val selector = ActorSelectorManager(dispatcher)
            this.server = aSocket(selector).tcp().bind(port = port)
        } catch (exception: Exception) {
            stop()
            throw exception
        }
        val server = server!!
        val scope = CoroutineScope(dispatcher)
        job = scope.launch {
            try {
                supervisorScope {
                    logger.info { "Listening for requests on port $port..." }
                    while (isActive) {
                        val socket = server.accept()
                        launch(dispatcher + exceptionHandler) {
                            logger.trace { "New connection accepted ${socket.remoteAddress}" }
                            val read = socket.openReadChannel()
                            val write = socket.openWriteChannel(autoFlush = false)
                            connect(read, write, socket.remoteAddress.toJavaAddress().hostname)
                        }
                    }
                }
            } finally {
                stop()
            }
        }
        return job!!
    }

    suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
        if (!connections.add(hostname)) {
            write.finish(Response.LOGIN_LIMIT_EXCEEDED)
            return
        }
        try {
            when (val opcode = read.readByte().toInt()) {
                Request.CONNECT_LOGIN -> loginServer?.connect(read, write, hostname)
                    ?: write.finish(Response.LOGIN_SERVER_OFFLINE)
                Request.CONNECT_JS5 -> fileServer.connect(read, write, hostname)
                else -> {
                    logger.trace { "Invalid sync session id: $opcode" }
                    write.finish(Response.INVALID_LOGIN_SERVER)
                }
            }
        } finally {
            connections.remove(hostname)
        }
    }

    fun stop() {
        job?.cancel()
        dispatcher?.close()
        server?.close()
        connections.clear()
    }

    companion object {

        @ExperimentalUnsignedTypes
        fun load(cache: Cache, properties: Properties): GameServer {
            val limit = properties.getProperty("network.maxClientPerIP").toInt()
            val fileServer = FileServer.load(cache, properties)
            return GameServer(fileServer, ConnectionTracker(limit))
        }

        private val logger = InlineLogger()
        private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            if (throwable is SocketException && throwable.message == "Connection reset") {
                logger.trace { "Connection reset: ${context.job}" }
            } else if (throwable is ClosedReceiveChannelException && throwable.message == "EOF while 1 bytes expected") {
                logger.trace { "EOF disconnection: ${context.job}" }
            } else {
                logger.error(throwable) { "Connection error" }
            }
        }
    }
}
