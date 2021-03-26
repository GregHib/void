package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractDialogue
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedIntMiddle

class DialogueContinueDecoder : Decoder(6) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val button = packet.readShortAdd()
        val packed = packet.readUnsignedIntMiddle()
        instructions.emit(InteractDialogue(Interface.getId(packed), Interface.getComponentId(packed), button))
    }

}