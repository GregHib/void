package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.ExamineNpc

class NPCExamineOptionDecoder : Decoder(2) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcId = packet.readShort().toInt()
        instructions.emit(ExamineNpc(npcId))
    }

}