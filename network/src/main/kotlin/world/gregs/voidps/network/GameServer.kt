package world.gregs.voidps.network

import com.github.michaelbull.logging.InlineLogger
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import world.gregs.voidps.cache.Cache
import java.net.SocketException
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * A network server for client's to connect to the game with
 */
class GameServer(
    private val gatekeeper: NetworkGatekeeper,
    private val loginLimit: Int,
    private val loginServer: Server,
    private val fileServer: Server
) {

    private lateinit var dispatcher: ExecutorCoroutineDispatcher
    private var running = false

    fun start(port: Int) = runBlocking {
        Runtime.getRuntime().addShutdownHook(thread(start = false) { stop() })
        val executor = Executors.newCachedThreadPool()
        dispatcher = executor.asCoroutineDispatcher()
        val selector = ActorSelectorManager(dispatcher)
        val supervisor = SupervisorJob()
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            if (throwable is SocketException && throwable.message == "Connection reset") {
                logger.trace { "Connection reset: ${context.job}" }
            } else if (throwable is ClosedReceiveChannelException && throwable.message == "EOF while 1 bytes expected") {
                logger.trace { "EOF disconnection: ${context.job}" }
            } else {
                logger.error(throwable) { "Connection error" }
            }
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
                launch(dispatcher) {
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
        when (val opcode = read.readByte().toInt()) {
            Request.CONNECT_LOGIN -> loginServer.connect(read, write, hostname)
            Request.CONNECT_JS5 -> fileServer.connect(read, write, hostname)
            else -> {
                logger.trace { "Invalid sync session id: $opcode" }
                write.finish(Response.INVALID_LOGIN_SERVER)
            }
        }
    }

    fun stop() {
        running = false
        dispatcher.close()
    }

    companion object {

        @ExperimentalUnsignedTypes
        fun load(cache: Cache, properties: Properties, gatekeeper: NetworkGatekeeper, loginServer: LoginServer): GameServer {
            val limit = properties.getProperty("loginLimit").toInt()
            val fileServer = FileServer.load(cache, properties)
            return GameServer(gatekeeper, limit, loginServer, fileServer)
        }

        private val logger = InlineLogger()
    }
}