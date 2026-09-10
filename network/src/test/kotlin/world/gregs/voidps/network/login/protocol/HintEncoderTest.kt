package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.IsaacCipher
import world.gregs.voidps.network.login.protocol.encode.arrowHint

class HintEncoderTest {

    /**
     * The client reads this packet as a fixed 12 bytes, so a shorter one swallows whatever
     * follows it on the wire and desynchronises the stream.
     */
    @TestFactory
    fun `Every hint arrow is the fixed packet length`() = listOf<Pair<String, Client.() -> Unit>>(
        "npc" to { arrowHint(type = 1, arrowIndex = 0, sprite = 0, entityIndex = 1234) },
        "player" to { arrowHint(type = 10, arrowIndex = 3, sprite = 0, entityIndex = 1234) },
        "tile" to { arrowHint(type = 2, arrowIndex = 1, sprite = 3, x = 100, y = 123, level = 2, z = 50, radius = 2) },
        "clear" to { arrowHint(type = 0, arrowIndex = 5) },
        "clear all slots" to { arrowHint(type = 0, arrowIndex = 7, sprite = -1) },
    ).map { (name, packet) ->
        dynamicTest("Test $name hint arrow length") {
            val channel = ByteChannel(true)
            val client = Client(channel, IsaacCipher(IntArray(4)), IsaacCipher(IntArray(4)), "")

            packet.invoke(client)

            val actual = ByteArray(channel.availableForRead)
            runTest {
                channel.readAvailable(actual)
            }
            assertEquals(PACKET_LENGTH + 1, actual.size, "$name hint arrow was ${actual.size - 1} bytes of payload")
        }
    }

    companion object {
        private const val PACKET_LENGTH = 12
    }
}
