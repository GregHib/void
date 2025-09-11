package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.login.protocol.Decoder

class FloorItemExamineDecoder : Decoder(2) {

    override suspend fun decode(packet: Source): Instruction {
        val itemId = packet.readShort().toInt()
        return ExamineItem(itemId)
    }
}
