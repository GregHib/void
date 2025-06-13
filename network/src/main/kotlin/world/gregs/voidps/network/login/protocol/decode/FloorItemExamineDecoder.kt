package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.login.protocol.Decoder

class FloorItemExamineDecoder : Decoder(2) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val itemId = packet.readShort().toInt()
        return ExamineItem(itemId)
    }
}
