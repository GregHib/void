package world.gregs.voidps.web.route

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.server.application.log
import io.ktor.server.plugins.origin
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Relays the web client's websocket to the game server over TCP.
 *
 * Uses non-blocking sockets so idle connections don't hold threads, and caps connections
 * per remote host and in total so the relay can't be used to exhaust server resources.
 * Game protocol frames aren't inspected; the game server authenticates them as it would any direct TCP client.
 */
internal fun Routing.proxy(host: String, port: Int, limits: ProxyLimits) {
    val selector = SelectorManager(Dispatchers.IO)
    webSocket("/proxy") {
        val remote = call.request.origin.remoteHost
        if (!limits.acquire(remote)) {
            close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "Too many connections"))
            return@webSocket
        }
        try {
            relay(selector, host, port)
        } finally {
            limits.release(remote)
        }
    }
}

private suspend fun DefaultWebSocketServerSession.relay(selector: SelectorManager, host: String, port: Int) {
    val socket: Socket = try {
        withTimeout(CONNECT_TIMEOUT.milliseconds) {
            aSocket(selector).tcp().connect(host, port) { noDelay = true }
        }
    } catch (e: IOException) {
        close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Could not reach TCP server: ${e.message}"))
        return
    } catch (e: TimeoutCancellationException) {
        close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Could not reach TCP server: timed out"))
        return
    }

    socket.use {
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = false)

        // TCP -> WebSocket
        val job = launch {
            val buffer = ByteArray(8192)
            try {
                while (isActive) {
                    val read = input.readAvailable(buffer)
                    if (read == -1) {
                        break
                    }
                    send(Frame.Binary(true, buffer.copyOf(read)))
                }
            } catch (e: IOException) {
                // socket closed / connection reset, fall through to clean up
            } catch (e: ClosedSendChannelException) {
                // websocket already closing, nothing to send to
            } catch (e: CancellationException) {
                throw e // don't swallow cancellation
            } catch (e: Exception) {
                call.application.log.warn("proxy TCP->WS pump failed for $host:$port", e)
            }
            // Game server closed the connection, close the websocket too
            close(CloseReason(CloseReason.Codes.NORMAL, "Server closed connection"))
        }

        // WebSocket -> TCP
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        output.writeFully(frame.readBytes())
                        output.flush()
                    }
                    is Frame.Close -> break
                    else -> {} // ignore text/ping/pong; engine handles ping/pong itself
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            // browser closed the WebSocket
        } catch (e: IOException) {
            // TCP write failed, e.g. server closed the socket
        } finally {
            job.cancel()
        }
    }
}

private const val CONNECT_TIMEOUT = 5000L
