package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.ExamineNpc

class NPCExamineDecoder : Decoder(2) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcId = packet.readShort().toInt()
        instructions.emit(ExamineNpc(npcId))
    }

}