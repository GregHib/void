package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.login.protocol.Decoder

class FloorItemExamineDecoder : Decoder(2) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val itemId = packet.readShort().toInt()
        instructions.emit(ExamineItem(itemId))
    }

}