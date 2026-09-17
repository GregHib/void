package world.gregs.voidps.web.route

import io.ktor.server.application.log
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

internal fun Routing.proxy(host: String, port: Int) {
    webSocket("/proxy") {
        val socket = try {
            withContext(Dispatchers.IO) {
                Socket().apply {
                    tcpNoDelay = true
                    soTimeout = 0

                    connect(InetSocketAddress(host, port), 5000)
                }
            }
        } catch (e: IOException) {
            close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Could not reach TCP server: ${e.message}"))
            return@webSocket
        }

        val input = socket.getInputStream()
        val output = socket.getOutputStream()

        // TCP -> WebSocket
        val job = launch(Dispatchers.IO) {
            val buffer = ByteArray(8192)
            try {
                while (isActive) {
                    val read = input.read(buffer)
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
        }

        // WebSocket -> TCP
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        output.write(frame.readBytes())
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
            withContext(Dispatchers.IO) {
                try {
                    socket.close()
                } catch (_: IOException) {
                }
            }
            job.join()
        }
    }
}