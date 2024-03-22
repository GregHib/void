package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.readBooleanAdd

class NPCOption4Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcIndex = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractNPC(npcIndex, 4))
    }

}