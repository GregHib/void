package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.*

class DecoderTest {

    @TestFactory
    fun `Test decoders`() = listOf(
        Triple(10, FriendAdd("friends_name"), byteArrayOf(102, 114, 105, 101, 110, 100, 115, 95, 110, 97, 109, 101, 0)),
        Triple(14, IgnoreAdd("ignore_name"), byteArrayOf(105, 103, 110, 111, 114, 101, 95, 110, 97, 109, 101, 0)),
        Triple(13, null, byteArrayOf(48, 57)),
        Triple(40, null, byteArrayOf(57, 48, 21, -72, -113, 25, 0, 0, -91, 110, 29, 102)),
        Triple(31, ChatTypeChange(123), byteArrayOf(123)),
        Triple(50, ClanChatJoin("clan chat"), byteArrayOf(99, 108, 97, 110, 32, 99, 104, 97, 116, 0)),
        Triple(64, ClanChatKick("clan chat"), byteArrayOf(99, 108, 97, 110, 32, 99, 104, 97, 116, 0)),
        Triple(1, null, byteArrayOf(0, 0, 4, -46, 99, 108, 97, 110, 32, 99, 104, 97, 116, 0)),
        Triple(74, ClanChatRank("friend name", 123), byteArrayOf(5, 102, 114, 105, 101, 110, 100, 32, 110, 97, 109, 101, 0)),
        Triple(53, ExecuteCommand("item", "abyssal_whip 1"), byteArrayOf(1, 2, 105, 116, 101, 109, 32, 97, 98, 121, 115, 115, 97, 108, 95, 119, 104, 105, 112, 32, 49, 0)),
        Triple(6, FriendDelete("friends_name"), byteArrayOf(102, 114, 105, 101, 110, 100, 115, 95, 110, 97, 109, 101, 0)),
        Triple(73, IgnoreDelete("ignore_name"), byteArrayOf(105, 103, 110, 111, 114, 101, 95, 110, 97, 109, 101, 0)),
        Triple(2, InteractDialogue(123, 456, 7), byteArrayOf(0, -121, 1, -56, 0, 123)),
        Triple(37, ExamineItem(12345), byteArrayOf(48, 57)),
        Triple(22, InteractFloorItem(12345, 5432, 6789, 0), byteArrayOf(48, 57, 21, 56, 26, -123, 127)),
        Triple(16, InteractFloorItem(12345, 5432, 6789, 1), byteArrayOf(26, 5, 48, -71, 56, 21, -1)),
        Triple(45, InteractFloorItem(12345, 5432, 6789, 2), byteArrayOf(48, 57, 21, -72, 1, 5, 26)),
        Triple(24, InteractFloorItem(12345, 5432, 6789, 3), byteArrayOf(127, 21, -72, -123, 26, 48, 57)),
        Triple(26, InteractFloorItem(12345, 5432, 6789, 4), byteArrayOf(26, -123, 21, -72, -1, 48, -71)),
        Triple(67, null, byteArrayOf(119, 119, 119, 46, 103, 111, 111, 103, 108, 101, 46, 99, 111, 109, 0, 115, 101, 97, 114, 99, 104, 0, 123)),
        Triple(32, EnterInt(12345), byteArrayOf(0, 0, 48, 57)),
        Triple(70, InterfaceClosedInstruction, byteArrayOf()),
        Triple(34, InteractInterfaceFloorItem(12345, 1234, 4321, 123, 456, 23456, 12), byteArrayOf(-46, 4, 48, -71, 12, 0, 16, -31, 1, 91, 32, 1, -56, 0, 123)),
        Triple(72, InteractInterfaceItem(12345, 23456, 12, 21, 123, 456, 654, 321), byteArrayOf(2, -114, 1, 65, 123, 0, -56, 1, 0, -116, 48, 57, 0, -107, 91, -96)),
        Triple(61, InteractInterfaceNPC(12345, 123, 456, 23456, 12), byteArrayOf(-116, 0, 0, 123, 1, -56, 57, 48, -127, 91, 32)),
        Triple(54, InteractInterfaceObject(12345, 1234, 4321, 123, 456, 23456, 12), byteArrayOf(91, -96, 82, 4, -56, 1, 123, 0, 16, 97, 127, 12, 0, 57, 48)),
        Triple(48, InteractInterfacePlayer(1234, 123, 456, 23456, 12), byteArrayOf(-116, 0, -46, 4, -96, 91, 123, 0, -56, 1, -1)),
        Triple(23, InteractInterface(123, 456, 23456, 12, 0), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(59, InteractInterface(123, 456, 23456, 12, 1), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(9, InteractInterface(123, 456, 23456, 12, 2), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(15, InteractInterface(123, 456, 23456, 12, 3), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(17, InteractInterface(123, 456, 23456, 12, 4), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(39, InteractInterface(123, 456, 23456, 12, 5), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(33, InteractInterface(123, 456, 23456, 12, 6), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(60, InteractInterface(123, 456, 23456, 12, 7), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(11, InteractInterface(123, 456, 23456, 12, 8), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(42, InteractInterface(123, 456, 23456, 12, 9), byteArrayOf(0, 123, 1, -56, 91, -96, 0, 12)),
        Triple(78, MoveInventoryItem(123, 456, 12345, 12, 654, 321, 23456, 21), byteArrayOf(0, 123, 1, -56, 21, 0, 1, 65, 2, -114, 91, -96, -116, 0, -71, 48)),
        Triple(69, null, byteArrayOf(1, 0, 123, 2, 1, 65)),
        Triple(75, null, byteArrayOf(123, -56, 21)),
        Triple(30, null, byteArrayOf(0, 1, -30, 64)),
        Triple(55, null, byteArrayOf(48, 57, 91, -96)),
        Triple(68, ExamineNpc(12345), byteArrayOf(48, 57)),
        Triple(63, InteractNPC(12345, 1), byteArrayOf(1, 57, 48)),
        Triple(29, InteractNPC(12345, 2), byteArrayOf(-71, 48, -127)),
        Triple(5, InteractNPC(12345, 3), byteArrayOf(48, 57, 1)),
        Triple(62, InteractNPC(12345, 4), byteArrayOf(57, 48, -127)),
        Triple(65, InteractNPC(12345, 5), byteArrayOf(-127, 57, 48)),
        Triple(46, ExamineObject(12345), byteArrayOf(48, 57)),
        Triple(27, InteractObject(12345, 1234, 4567, 1), byteArrayOf(127, 82, 4, -41, 17, 48, 57)),
        Triple(36, InteractObject(12345, 1234, 4567, 2), byteArrayOf(87, 17, 4, 82, 127, -71, 48)),
        Triple(80, InteractObject(12345, 1234, 4567, 3), byteArrayOf(17, 87, -71, 48, -46, 4, -127)),
        Triple(56, InteractObject(12345, 1234, 4567, 4), byteArrayOf(-127, 48, -71, 4, 82, -41, 17)),
        Triple(38, InteractObject(12345, 1234, 4567, 5), byteArrayOf(-41, 17, -127, 82, 4, 48, -71)),
        Triple(0, null, byteArrayOf()),
        Triple(25, InteractPlayer(1234, 1), byteArrayOf(82, 4, -1)),
        Triple(12, InteractPlayer(1234, 2), byteArrayOf(1, 4, -46)),
        Triple(79, InteractPlayer(1234, 3), byteArrayOf(127, -46, 4)),
        Triple(44, InteractPlayer(1234, 4), byteArrayOf(4, -46, -127)),
        Triple(81, InteractPlayer(1234, 5), byteArrayOf(4, 82, 1)),
        Triple(51, InteractPlayer(1234, 6), byteArrayOf(1, 4, 82)),
        Triple(57, InteractPlayer(1234, 7), byteArrayOf(4, 82, -127)),
        Triple(18, InteractPlayer(1234, 8), byteArrayOf(1, 4, 82)),
        Triple(20, ChatPrivate("name", "message"), byteArrayOf(110, 97, 109, 101, 0, 7, -95, -18, -94, -52)),
        Triple(19, QuickChatPrivate("name", 1234, byteArrayOf(123)), byteArrayOf(110, 97, 109, 101, 0, 4, -46, 123)),
        Triple(41, ChatPublic("message", 31500), byteArrayOf(123, 12, 7, -95, -18, -94, -52)),
        Triple(3, QuickChatPublic(12, 1234, byteArrayOf(123)), byteArrayOf(12, 4, -46, 123)),
        Triple(77, null, byteArrayOf(123)),
        Triple(4, FinishRegionLoad, byteArrayOf()),
        Triple(47, null, byteArrayOf(0, 0, 48, 57)),
        Triple(66, ReportAbuse("name", 12, 123, "message"), byteArrayOf(110, 97, 109, 101, 0, 12, 123, 109, 101, 115, 115, 97, 103, 101, 0)),
        Triple(28, null, byteArrayOf(48, 57)),
        Triple(7, ChangeDisplayMode(12, 345, 678, 9), byteArrayOf(12, 1, 89, 2, -90, 9)),
        Triple(76, null, byteArrayOf(82, 4, -31, 16)),
        Triple(43, EnterString("a string"), byteArrayOf(97, 32, 115, 116, 114, 105, 110, 103, 0)),
        Triple(84, null, byteArrayOf(123)),
        Triple(71, null, byteArrayOf(4, -46)),
        Triple(35, Walk(1234, 4321), byteArrayOf(-31, 16, -127, 4, 82)),
        Triple(82, Walk(1234, 4321), byteArrayOf(-31, 16, -127, 4, 82, -1, -1, 48, 57, 123, 12, 21, 23, 1, 65, 1, -80, 32)),
        Triple(49, null, byteArrayOf(48, 57, 0, 0, -44, 49)),
        Triple(8, null, byteArrayOf(1)),
        Triple(52, null, byteArrayOf(0, 0, 48, 57)),
        Triple(58, null, byteArrayOf(0, 0, 48, 57)),
    ).map { (id, expected, data) ->
        dynamicTest("Test packet $id ${if (expected != null) expected::class.simpleName else "Interaction"} decoder") {
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
                val instructions = MutableSharedFlow<Instruction>(replay = 1)
                val packet = ByteReadPacket(data)
                decoder.decode(instructions, packet)
                val instruction = instructions.replayCache.firstOrNull()
                assertEquals(expected, instruction)
                assertTrue(packet.isEmpty)
            }
        }
    }

    companion object {
        private val huffman = Huffman()

        init {
            val data = ByteArray(256) { 22 }
            val array = byteArrayOf(16, 17, 7, 13, 13, 13, 16, 7, 10, 6, 16, 10, 11, 12, 12, 12, 12, 13, 13, 14, 14, 11, 14, 19, 15, 17, 8, 11, 9, 10, 10, 10, 10, 11, 10, 9, 7, 12, 11, 10, 10, 9, 10, 10, 12, 10, 9, 8, 12, 12, 9, 14, 8, 12, 17, 16, 17, 22, 13, 21, 4, 7, 6, 5, 3, 6, 6, 5, 4, 10, 7, 5, 6, 4, 4, 6, 10, 5, 4, 4, 5, 7, 6, 10, 6, 10, 22, 19, 22, 14)
            for (i in array.indices) {
                data[i + 37] = array[i]
            }
            data[6] = 21
            data[9] = 20
            data[13] = 21
            data[32] = 3
            data[33] = 8
            data[35] = 16
            data[247] = 21
            data[249] = 21
            data[253] = 21
            huffman.load(data)
        }
    }
}