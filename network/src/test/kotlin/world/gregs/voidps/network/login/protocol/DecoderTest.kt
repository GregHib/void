package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.core.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.*
import java.io.File

class DecoderTest {

    private val packets = File("./src/test/resources/decoder-packets.csv")
        .readLines()
        .map { line ->
            val parts = line.split(", ")
            parts[0].toInt() to parts.drop(1).mapNotNull { if (it.isEmpty()) null else it.toByte() }.toByteArray()
        }

    private val huffman = Huffman()

    init {
        val data = File("./src/test/resources/huffman.csv").readText().split(", ").map { it.toByte() }.toByteArray()
        huffman.load(data)
    }

    @TestFactory
    fun `Test decoders`() = listOf(
        FriendAdd("friends_name"), // 10
        IgnoreAdd("ignore_name"), // 14
        null, // 13
        null, // 40
        ChatTypeChange(123), // 31
        ClanChatJoin("clan chat"), // 50
        ClanChatKick("clan chat"), // 64
        null, // 1
        ClanChatRank("friend name", 123), // 74
        ExecuteCommand("item", "abyssal_whip 1"), // 53
        FriendDelete("friends_name"), // 6
        IgnoreDelete("ignore_name"), // 73
        InteractDialogue(123, 456, 7), // 2
        ExamineItem(12345), // 37
        InteractFloorItem(12345, 5432, 6789, 0), // 22
        InteractFloorItem(12345, 5432, 6789, 1), // 16
        InteractFloorItem(12345, 5432, 6789, 2), // 45
        InteractFloorItem(12345, 5432, 6789, 3), // 24
        InteractFloorItem(12345, 5432, 6789, 4), // 26
        null, // 67
        EnterInt(12345), // 32
        InterfaceClosedInstruction, // 70
        InteractInterfaceFloorItem(12345, 1234, 4321, 123, 456, 23456, 12), // 34
        InteractInterfaceItem(12345, 23456, 12, 21, 123, 456, 654, 321), // 72
        InteractInterfaceNPC(12345, 123, 456, 23456, 12), // 61
        InteractInterfaceObject(12345, 1234, 4321, 123, 456, 23456, 12), // 54
        InteractInterfacePlayer(1234, 123, 456, 23456, 12), // 48
        InteractInterface(123, 456, 23456, 12, 0), // 23
        InteractInterface(123, 456, 23456, 12, 1), // 59
        InteractInterface(123, 456, 23456, 12, 2), // 9
        InteractInterface(123, 456, 23456, 12, 3), // 15
        InteractInterface(123, 456, 23456, 12, 4), // 17
        InteractInterface(123, 456, 23456, 12, 5), // 39
        InteractInterface(123, 456, 23456, 12, 6), // 33
        InteractInterface(123, 456, 23456, 12, 7), // 60
        InteractInterface(123, 456, 23456, 12, 8), // 11
        InteractInterface(123, 456, 23456, 12, 9), // 42
        MoveInventoryItem(123, 456, 12345, 12, 654, 321, 23456, 21), // 78
        null, // 69
        null, // 75
        null, // 30
        null, // 55
        ExamineNpc(12345), // 68
        InteractNPC(12345, 1), // 63
        InteractNPC(12345, 2), // 29
        InteractNPC(12345, 3), // 5
        InteractNPC(12345, 4), // 62
        InteractNPC(12345, 5), // 65
        ExamineObject(12345), // 46
        InteractObject(12345, 1234, 4567, 1), // 27
        InteractObject(12345, 1234, 4567, 2), // 36
        InteractObject(12345, 1234, 4567, 3), // 80
        InteractObject(12345, 1234, 4567, 4), // 56
        InteractObject(12345, 1234, 4567, 5), // 38
        null, // 0
        InteractPlayer(1234, 1), // 25
        InteractPlayer(1234, 2), // 12
        InteractPlayer(1234, 3), // 79
        InteractPlayer(1234, 4), // 44
        InteractPlayer(1234, 5), // 81
        InteractPlayer(1234, 6), // 51
        InteractPlayer(1234, 7), // 57
        InteractPlayer(1234, 8), // 18
        ChatPrivate("name", "message"), // 20
        QuickChatPrivate("name", 1234, byteArrayOf(123)), // 19
        ChatPublic("message", 31500), // 41
        QuickChatPublic(12, 1234, byteArrayOf(123)), // 3
        null, // 77
        FinishRegionLoad, // 4
        null, // 47
        ReportAbuse("name", 12, 123, "message"), // 66
        null, // 28
        ChangeDisplayMode(12, 345, 678, 9), // 7
        null, // 76
        EnterString("a string"), // 43
        null, // 84
        null, // 71
        Walk(1234, 4321), // 35
        Walk(1234, 4321, minimap = true), // 82
        null, // 49
        null, // 8
        SongEnd(12345), // 52
        WorldMapClick(tile = 12345), // 58
    ).mapIndexed { index, expected ->
        val (id, data) = packets[index]
        dynamicTest("Test ${if (expected != null) expected::class.simpleName else "Packet $id"} decoder") {
            runTest {
                val decoders = decoders(huffman)
                val decoder = decoders[id]
                assertNotNull(decoder)
                val size = decoder!!.length
                when (size) {
                    Decoder.BYTE -> assertTrue(data.size <= Byte.MAX_VALUE)
                    Decoder.SHORT -> assertTrue(data.size <= Short.MAX_VALUE)
                    else -> assertEquals(decoder.length, data.size)
                }
                val instructions = Channel<Instruction>(capacity = 1)
                val packet = ByteReadPacket(data)
                instructions.send(decoder.decode(packet) ?: return@runTest)
                val instruction = instructions.tryReceive().getOrNull()
                assertEquals(expected, instruction)
                assertTrue(packet.isEmpty)
            }
        }
    }
}