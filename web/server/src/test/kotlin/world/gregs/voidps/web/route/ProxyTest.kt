package world.gregs.voidps.web.route

import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import io.ktor.client.plugins.websocket.WebSockets as ClientWebSockets
import io.ktor.server.websocket.WebSockets as ServerWebSockets

class ProxyTest {

    private lateinit var echo: ServerSocket

    @BeforeEach
    fun setup() {
        echo = ServerSocket(0)
        thread(isDaemon = true) {
            while (!echo.isClosed) {
                val socket = try {
                    echo.accept()
                } catch (_: Exception) {
                    break
                }
                thread(isDaemon = true) {
                    socket.use { it.getInputStream().copyTo(it.getOutputStream()) }
                }
            }
        }
    }

    @AfterEach
    fun teardown() {
        echo.close()
    }

    private fun ApplicationTestBuilder.proxyApp(limits: ProxyLimits) {
        application {
            install(ServerWebSockets)
            routing { proxy("localhost", echo.localPort, limits) }
        }
    }

    @Test
    fun `Relays bytes to and from the tcp server`() = testApplication {
        val limits = ProxyLimits(maxPerHost = 5, maxTotal = 5)
        proxyApp(limits)
        val client = createClient { install(ClientWebSockets) }
        client.webSocket("/proxy") {
            send(Frame.Binary(true, byteArrayOf(1, 2, 3)))
            val frame = incoming.receive()
            assertContentEquals(byteArrayOf(1, 2, 3), frame.readBytes())
            assertEquals(1, limits.total())
        }
    }

    @Test
    fun `Connections over the limit are refused`() = testApplication {
        val limits = ProxyLimits(maxPerHost = 1, maxTotal = 5)
        proxyApp(limits)
        val client = createClient { install(ClientWebSockets) }
        client.webSocket("/proxy") {
            send(Frame.Binary(true, byteArrayOf(1)))
            incoming.receive()
            client.webSocket("/proxy") {
                assertEquals(CloseReason.Codes.TRY_AGAIN_LATER, closeReason.await()?.knownReason)
            }
        }
    }
}
