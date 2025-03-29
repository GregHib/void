package world.gregs.voidps.network.login.protocol

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.IsaacCipher
import world.gregs.voidps.network.login.protocol.encode.*
import world.gregs.voidps.network.login.protocol.encode.zone.*
import java.io.File
import kotlin.random.Random

class EncodersTest {

    private val zonePackets = File("./src/test/resources/encoder-zone-packets.csv")
        .readLines()
        .map { line -> line.split(", ").map { it.toByte() }.toByteArray() }

    @TestFactory
    fun `Test zone encoders`() = listOf(
        FloorItemAddition(123456, 12345, 23456, null),
        FloorItemRemoval(123456, 12345, null),
        FloorItemReveal(123456, 12345, 23456, 1234),
        FloorItemUpdate(123456, 12345, 23456, 1234, null),
        GraphicAddition(123456, 12345, 123, 1234, 12),
        MidiAddition(123456, 12345, 12, 21, 23, 32, 123),
        ObjectAddition(123456, 12345, 12, 21),
        ObjectAnimation(123456, 12345, 12, 21),
        ObjectRemoval(123456, 12, 21),
        ProjectileAddition(123456, 12345, 1234, 12, 21, 23, 32, 123, 321, 34, 321),
        SoundAddition(123456, 12345, 12, 21, 123, 23, 1234),
    ).mapIndexed { index, update ->
        val expected = zonePackets[index]
        dynamicTest("Test ${update::class.simpleName} encoder") {
            runTest {
                val channel = ByteArrayChannel()
                channel.encode(update)
                val actual = channel.toByteArray()
                assertContentEquals(expected, actual)
            }
        }
    }

    private val packets = File("./src/test/resources/encoder-packets.csv")
        .readLines()
        .map { line ->
            val parts = line.split(", ")
            parts[0] to parts.drop(1).map { it.toByte() }.toByteArray()
        }

    @TestFactory
    fun `Test encoders`() =
        listOf<Client.() -> Unit>(
            { moveCamera(123, 234, 1234, 23, 32) },
            { turnCamera(123, 234, 1234, 23, 32) },
            { shakeCamera(123, 234, 1234, 23, 32) },
            { clearCamera() },
            { message("message", 123, 123456, "name", "formatted") },
            { publicChat(1234, 4321, 123, "message".toByteArray()) },
            { publicQuickChat(1234, 4321, 123, 12345, byteArrayOf(12, 21, 23)) },
            { privateChatFrom("display", 123, "message".toByteArray(), "name") },
            { privateQuickChatFrom("display", 123, 1234, byteArrayOf(12, 21, 23)) },
            { privateChatTo("display", "message".toByteArray()) },
            { privateQuickChatTo("display", 1234, "message".toByteArray()) },
            { sendPublicStatus(123, 134) },
            { sendPrivateStatus(123) },
            { clanChat("display", "clan", 123, "message".toByteArray(), "response") },
            { clanQuickChat("display", "clan", 123, 1234, "message".toByteArray(), "response") },
            { appendClanChat(Member("display", 1234, 123, "world", "response")) },
            { updateClanChat("display", "clan", 123, listOf(Member("member", 1234, 123, "world", "response"))) },
            { leaveClanChat() },
            { sendInventoryItems(1234, 2, intArrayOf(1, 2, 3, 4), true) },
            { contextMenuOption("option", 123, true, 1234) },
            { sendFriendsList(listOf(Friend("name", "previous", 123, true, 12, "world", true))) },
            { sendIgnoreList(listOf("name" to "previous")) },
            { updateIgnoreList("name", "previous", true) },
            { animateInterface(InterfaceDefinition.pack(123, 345), 12345) },
            { closeInterface(InterfaceDefinition.pack(123, 345)) },
            { colourInterface(InterfaceDefinition.pack(123, 345), 123, 234, 255) },
            { colourInterface(InterfaceDefinition.pack(123, 345), 12345) },
            { npcDialogueHead(InterfaceDefinition.pack(123, 345), 12345) },
            { playerDialogueHead(InterfaceDefinition.pack(123, 345)) },
            { interfaceItem(InterfaceDefinition.pack(123, 345), 12345, 23456) },
            { sendInterfaceItemUpdate(123, listOf(Triple(1234, 12345, 23456), Triple(123, 12345, 234)), false) },
            { openInterface(true, InterfaceDefinition.pack(123, 345), 12345) },
            { sendInterfaceSettings(InterfaceDefinition.pack(1234, 456), 2345, 6789, 12345) },
            { sendInterfaceScroll(InterfaceDefinition.pack(123, 456), 23456) },
            { interfaceSprite(InterfaceDefinition.pack(123, 456), 12345) },
            { interfaceText(InterfaceDefinition.pack(123, 456), "text") },
            { updateInterface(123, 456) },
            { interfaceVisibility(InterfaceDefinition.pack(123, 456), true) },
            { login("name", 1234, 123, member = false, membersWorld = false) },
            { logout() },
            { mapRegion(123, 456, true, 12, arrayOf(IntArray(4)), 1234, 123456, intArrayOf(4321)) },
            { dynamicMapRegion(123, 456, true, 12, listOf(432, 1), arrayOf(IntArray(4)), 1234, 123456, intArrayOf(4321)) },
            { sendMinimapState(123) },
            { updateNPCs(BufferWriter(10), BufferWriter(10)) },
            { animateObject(123456, 12345, 12, 21) },
            { preloadObject(12345, 123) },
            { updatePlayers(BufferWriter(10), BufferWriter(10)) },
            { addProjectileHalfTile(123, 12345, 12, 21, 1234, 4321, 23, 32, 234, 432, 45, 5432) },
            { sendRunEnergy(123) },
            { sendScript(12345, listOf(null, 123, "string")) },
            { skillLevel(123, 99, 123456) },
            { playMusicTrack(12345, 123, 255) },
            { playSoundEffect(12345, 23456, 123, 4567, 123) },
            { playMIDI(12345, 23456, 123, 4321, 234) },
            { playJingle(12345, 123) },
            { tileText(123456, 12345, 123, 654321, "message") },
            { sendVarbit(12345, 123) },
            { sendVarbit(12345, 1234) },
            { sendVarc(12345, 123) },
            { sendVarc(12345, 1234) },
            { sendVarcStr(12345, "message") },
            { sendVarp(12345, 123) },
            { sendVarp(12345, 1234) },
            { weight(12345) },
            { clearZone(123, 234, 12) },
            { updateZone(123, 234, 12) },
        ).mapIndexed { index, packet ->
            random = Random(0)
            val (name, expected) = packets[index]
            dynamicTest("Test $name encoder") {
                val channel = ByteArrayChannel()
                val client = Client(channel, IsaacCipher(IntArray(4)), IsaacCipher(IntArray(4)), "")

                packet.invoke(client)

                val actual = channel.toByteArray()
                assertContentEquals(expected, actual)
            }
        }

    private fun assertContentEquals(expected: ByteArray, actual: ByteArray) {
        assertTrue(expected.contentEquals(actual)) { "Expected: ${expected.contentToString()}, Actual: ${actual.contentToString()}" }
    }
}