package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractNPC
import world.gregs.voidps.network.readBoolean

class NPCOption3Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcIndex = packet.readShort().toInt()
        val run = packet.readBoolean()
        instructions.emit(InteractNPC(npcIndex, 3))
    }

}