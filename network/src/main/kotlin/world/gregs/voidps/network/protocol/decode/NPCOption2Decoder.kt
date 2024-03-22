package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanAdd
import world.gregs.voidps.network.protocol.readShortAddLittle

class NPCOption2Decoder : Decoder(3) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val npcIndex = packet.readShortAddLittle()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractNPC(npcIndex, 2))
    }

}