import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration.Companion.seconds

private const val TCP_HOST = "localhost"
private const val TCP_PORT = 43594
private const val BRIDGE_PORT = 8081
private const val BRIDGE_PATH = "/bridge"

fun main() {
    embeddedServer(Netty, port = BRIDGE_PORT, host = "0.0.0.0") {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 30.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        routing {
            webSocket(BRIDGE_PATH) {
                // One raw TCP connection per browser WebSocket connection.
                val socket = try {
                    Socket().apply {
                        connect(InetSocketAddress(TCP_HOST, TCP_PORT), 5000)
                    }
                } catch (e: IOException) {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Could not reach TCP server: ${e.message}"))
                    return@webSocket
                }

                println("Socket opened.")
                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                // Coroutine 1: TCP -> WebSocket
                // Reads raw bytes from the TCP server and forwards them to the browser as binary frames.
                val tcpToWs = launch(Dispatchers.IO) {
                    val buffer = ByteArray(8192)
                    try {
                        while (isActive) {
                            val read = input.read(buffer)
                            if (read == -1) break // TCP server closed the connection
                            send(Frame.Binary(true, buffer.copyOf(read)))
                        }
                    } catch (e: IOException) {
                        // socket closed / connection reset, fall through to cleanup
                    }
                }

                // Coroutine 2: WebSocket -> TCP
                // Reads frames from the browser and writes the payload bytes to the TCP server.
                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Binary -> output.write(frame.readBytes())
                            is Frame.Close -> break
                            else -> { /* ignore ping/pong, handled automatically by the plugin */ }
                        }
                        output.flush()
                    }
                } catch (e: ClosedReceiveChannelException) {
                    // browser closed the WebSocket
                } catch (e: IOException) {
                    // TCP write failed, e.g. server closed the socket
                } finally {
                    tcpToWs.cancel()
                    withContext(Dispatchers.IO) {
                        try { socket.close() } catch (_: IOException) {}
                    }
                }
            }
        }
    }.start(wait = true)
}
 